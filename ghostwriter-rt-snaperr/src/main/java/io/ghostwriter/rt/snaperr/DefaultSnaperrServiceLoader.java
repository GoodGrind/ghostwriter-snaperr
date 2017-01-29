package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;
import io.ghostwriter.rt.snaperr.handler.NoOpHandler;
import io.ghostwriter.rt.snaperr.serializer.NoOpSerializer;

/**
 * Default implementation of SnaperrServiceLoader returns:
 * <li>{@link NoOpHandler}
 * <li>{@link NoOpSerializer}
 * 
 * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader
 */
public class DefaultSnaperrServiceLoader implements SnaperrServiceLoader {

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#load(java.lang.Class, io.ghostwriter.rt.snaperr.ConfigurationReader)
     * @return
     * <li>{@link NoOpHandler}
     * <li>{@link NoOpSerializer}
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
	else {
	    return null;
	}
    }

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#isServiceSupported(java.lang.Class)
     */
    @Override
    public boolean isServiceSupported(Class<?> clazz) {
	return TriggerSerializer.class.equals(clazz) || TriggerHandler.class.equals(clazz);
    }

}
