package io.ghostwriter.rt.snaperr.slf4j;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;

/**
 * Default implementation of SnaperrServiceLoader returns:
 * <li>{@link Slf4jHandler}
 * 
 * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader
 */
public class Slf4jSnaperrServiceLoader implements SnaperrServiceLoader {

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#load(java.lang.Class, io.ghostwriter.rt.snaperr.ConfigurationReader)
     * @return
     * <li>{@link Slf4jHandler}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(Class<T> clazz, ConfigurationReader configReader) {
	
	if (TriggerHandler.class.equals(clazz)) {

	    final TriggerHandler<String> triggerHandler = new Slf4jHandler();

	    return (T) triggerHandler;
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
	return TriggerHandler.class.equals(clazz);
    }

}
