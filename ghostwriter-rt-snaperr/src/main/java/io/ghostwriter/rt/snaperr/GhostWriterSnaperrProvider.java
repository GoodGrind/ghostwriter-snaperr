package io.ghostwriter.rt.snaperr;

import java.util.Properties;
import java.util.ServiceLoader;

import io.ghostwriter.TracerProvider;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;
import io.ghostwriter.rt.snaperr.handler.Slf4jTriggerHandlerServiceLoader;
import io.ghostwriter.rt.snaperr.serializer.JsonSerializerServiceLoader;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;

public class GhostWriterSnaperrProvider implements TracerProvider<SnaperrTracer> {

    private static final Logger LOG = Logger.getLogger(GhostWriterSnaperrProvider.class.getName());

    private SnaperrTracer ghostWriterSnaperr;
    private boolean gwSetupSuccessful;

    @Override
    public SnaperrTracer getTracer() {
	synchronized (this) {

	    if (gwSetupSuccessful) {
		return ghostWriterSnaperr;
	    }

	    try {
		final ConfigurationReader configReader = new ConfigurationReader();

		@SuppressWarnings("rawtypes")
		final TriggerSerializer triggerSerializer = loadSnaperrService(TriggerSerializer.class, configReader, null);
		final TriggerHandler triggerHandler = loadSnaperrService(TriggerHandler.class, configReader, triggerSerializer);

		final String gwAppName = configReader.getGwAppName();
		final Properties gwProperties = configReader.getGwProperties(gwAppName);

		final String strThrottleWindowSize = gwProperties.getProperty(
			ConfigurationReader.CFG_THROTTLE_WINDOW_SIZE,
			String.valueOf(SnaperrTracer.DEFAULT_ERROR_WINDOW_SIZE_MS));
		final long throttleWindowSize = Long.parseLong(strThrottleWindowSize);

		final String strThrottleMaxErrorsInWindow = gwProperties.getProperty(
			ConfigurationReader.CFG_THROTTLE_MAX_ERRORS,
			String.valueOf(SnaperrTracer.DEFAULT_MAX_ERROR_COUNT_IN_WINDOW));
		final int throttleMaxErrorsInWindow = Integer.parseInt(strThrottleMaxErrorsInWindow);

		final ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
		ghostWriterSnaperr = new SnaperrTracer(referenceTracker, triggerHandler, throttleWindowSize,
			throttleMaxErrorsInWindow);

		gwSetupSuccessful = true;

		return ghostWriterSnaperr;

	    }
	    catch (Exception e) {
		LOG.error("GhostWriter configuration failed, skip using GhostWriter: " + e.getMessage(), e);
		gwSetupSuccessful = false;
		return null;
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private static <T> T loadSnaperrService(final Class<T> clazz, final ConfigurationReader configReader,
	    @SuppressWarnings("rawtypes") final TriggerSerializer triggerSerializer) {
	
	@SuppressWarnings("rawtypes")
	ServiceLoader<SnaperrServiceLoader> snaperrServiceLoaderList = ServiceLoader.load(SnaperrServiceLoader.class);

	T loadedService = null;
	for ( SnaperrServiceLoader<T> snaperrServiceLoader : snaperrServiceLoaderList) {
	    final boolean isServiceSupported = snaperrServiceLoader.isServiceSupported(clazz);

	    if (!isServiceSupported) {
		LOG.debug("Snaperr service loader " + snaperrServiceLoader.getClass().getName()
			+ " does not support service " + clazz.getName() + ": Skipping");

		continue;
	    }

	    if (loadedService != null && isServiceSupported) {
		LOG.info("An implementation of " + clazz.getName()
			+ " service has already been loaded. Skipping loading other implementation by Service loader: "
			+ snaperrServiceLoader.getClass());
	    }
	    else if (loadedService == null && isServiceSupported) {
		LOG.info("Loading implementation of " + clazz.getName() + " by "
			+ snaperrServiceLoader.getClass().getName() + " service found");

		final T tmpLoadedService = (T) snaperrServiceLoader.load(clazz, configReader, triggerSerializer);

		if (tmpLoadedService == null) {
		    throw new IllegalStateException("Service loader " + snaperrServiceLoader.getClass().getName()
			    + " supports " + clazz.getName() + " but retunred NULL");
		}
		else {
		    LOG.info("Loaded service: " + tmpLoadedService.getClass().getName());
		}
	    }
	}

	if (loadedService == null) {
	    LOG.warn("No implementation of " + clazz.getName() + " found, loading default implementation");
	    loadedService = loadDefaultSnaperrService(clazz, configReader, triggerSerializer);
	}

	return loadedService;
    }

    @SuppressWarnings("unchecked")
    private static <T> T loadDefaultSnaperrService(final Class<T> clazz, final ConfigurationReader configReader,
	    @SuppressWarnings("rawtypes") final TriggerSerializer triggerSerializer) {

	SnaperrServiceLoader<T> snaperrServiceLoader = null;
	if (TriggerHandler.class.equals(clazz)) {
	    snaperrServiceLoader = (SnaperrServiceLoader<T>) new Slf4jTriggerHandlerServiceLoader();
	}
	else if (TriggerSerializer.class.equals(clazz)) {
	    snaperrServiceLoader = (SnaperrServiceLoader<T>) new JsonSerializerServiceLoader();
	}
	else {
	    throw new RuntimeException("Service " + clazz.getName() + " is not recognized by Snaperr. Use "
		    + TriggerHandler.class.getName() + " or " + TriggerSerializer.class.getName());
	}

	T loadedService = snaperrServiceLoader.load(clazz, configReader, triggerSerializer);
	return loadedService;
    }

}
