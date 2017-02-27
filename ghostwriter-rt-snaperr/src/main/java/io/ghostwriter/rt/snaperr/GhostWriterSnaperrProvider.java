package io.ghostwriter.rt.snaperr;

import java.util.Properties;
import java.util.ServiceLoader;

import io.ghostwriter.TracerProvider;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.ThrottleControlStrategy;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;
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
		final TriggerSerializer triggerSerializer = loadSnaperrService(TriggerSerializer.class, configReader);
		final TriggerHandler triggerHandler = loadSnaperrService(TriggerHandler.class, configReader);
		final ThrottleControlStrategy throttleControl = loadSnaperrService(ThrottleControlStrategy.class, configReader);

		final ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
		ghostWriterSnaperr = new SnaperrTracer(referenceTracker, triggerSerializer, triggerHandler, throttleControl);

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

    private static <T> T loadSnaperrService(final Class<T> clazz, final ConfigurationReader configReader) {
	
	ServiceLoader<SnaperrServiceLoader> snaperrServiceLoaderList = ServiceLoader.load(SnaperrServiceLoader.class);

	T loadedService = null;
	for ( SnaperrServiceLoader snaperrServiceLoader : snaperrServiceLoaderList) {
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

		final T tmpLoadedService = (T) snaperrServiceLoader.load(clazz, configReader);

		if (tmpLoadedService == null) {
		    throw new IllegalStateException("Service loader " + snaperrServiceLoader.getClass().getName()
			    + " supports " + clazz.getName() + " but returned NULL");
		}
		else {
		    loadedService = tmpLoadedService;
		    LOG.info("Loaded service: " + tmpLoadedService.getClass().getName());
		}
	    }
	}

	if (loadedService == null) {
	    LOG.warn("No implementation of " + clazz.getName() + " found, loading default implementation");
	    loadedService = loadDefaultSnaperrService(clazz, configReader);
	}

	return loadedService;
    }

    private static <T> T loadDefaultSnaperrService(final Class<T> clazz, final ConfigurationReader configReader) {
	DefaultSnaperrServiceLoader defaultServiceLoader = new DefaultSnaperrServiceLoader();
	
	if (defaultServiceLoader.isServiceSupported(clazz)) {
	    return defaultServiceLoader.load(clazz, configReader);
	}
	else {
	    throw new RuntimeException("Service " + clazz.getName() + " is not recognized by Snaperr. Use "
		    + TriggerHandler.class.getName() + " or " + TriggerSerializer.class.getName());
	}
    }

}
