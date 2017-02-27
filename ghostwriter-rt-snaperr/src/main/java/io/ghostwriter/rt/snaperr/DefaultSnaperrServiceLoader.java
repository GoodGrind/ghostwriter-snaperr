package io.ghostwriter.rt.snaperr;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.ThrottleControlStrategy;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;
import io.ghostwriter.rt.snaperr.handler.NoOpHandler;
import io.ghostwriter.rt.snaperr.serializer.NoOpSerializer;
import io.ghostwriter.rt.snaperr.tracker.DefaultThrottleControlStrategy;

/**
 * Default implementation of SnaperrServiceLoader returns:
 * <li>{@link NoOpHandler}
 * <li>{@link NoOpSerializer}
 * 
 * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader
 */
public class DefaultSnaperrServiceLoader implements SnaperrServiceLoader {

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#load(java.lang.Class,
     *      io.ghostwriter.rt.snaperr.ConfigurationReader)
     * @return
     *         <li>{@link NoOpHandler}
     *         <li>{@link NoOpSerializer}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(Class<T> clazz, ConfigurationReader configReader) {

	if (TriggerSerializer.class.equals(clazz)) {
	    final TriggerSerializer<String> result = new NoOpSerializer();
	    return (T) result;
	}
	else if (TriggerHandler.class.equals(clazz)) {

	    final TriggerHandler<String> triggerHandler = new NoOpHandler();

	    return (T) triggerHandler;
	}
	else if (ThrottleControlStrategy.class.equals(clazz)) {

	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);
	    final String strThrottleWindowSize = gwProperties.getProperty(ConfigurationReader.CFG_THROTTLE_WINDOW_SIZE,
		    String.valueOf(SnaperrTracer.DEFAULT_ERROR_WINDOW_SIZE_MS));
	    final long throttleWindowSize = Long.parseLong(strThrottleWindowSize);

	    final String strThrottleMaxErrorsInWindow = gwProperties.getProperty(
		    ConfigurationReader.CFG_THROTTLE_MAX_ERRORS,
		    String.valueOf(SnaperrTracer.DEFAULT_MAX_ERROR_COUNT_IN_WINDOW));
	    final int throttleMaxErrorsInWindow = Integer.parseInt(strThrottleMaxErrorsInWindow);

	    final DefaultThrottleControlStrategy throttleControl = new DefaultThrottleControlStrategy(throttleWindowSize,
		    throttleMaxErrorsInWindow);

	    return (T) throttleControl;
	}
	else {
	    return null;
	}
    }

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#isServiceSupported(java.lang.Class)
     */
    @Override
    public boolean isServiceSupported(Class<?> clazz) {
	return TriggerSerializer.class.equals(clazz) || TriggerHandler.class.equals(clazz) || ThrottleControlStrategy.class.equals(clazz);
    }

}
