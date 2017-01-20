package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;

/**
 * Default implementation of SnaperrServiceLoader returns null
 * @see io.ghostwriter.rt.snaperr.SnaperrServiceLoader
 */
public class DefaultSnaperrServiceLoader implements SnaperrServiceLoader<Object> {

    @Override
    public Object load(Class<Object> clazz, ConfigurationReader configReader, TriggerSerializer triggerSerializer) {
	return isServiceSupported(clazz) ? null : null;
    }

    @Override
    public boolean isServiceSupported(Class<Object> clazz) {
	return false;
    }

}
