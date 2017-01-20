package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;

/**
 * Load a Snaperr service provided service interface name and Configuration reader
 *
 * @param <T>
 */
public interface SnaperrServiceLoader<T> {
    
    /**
     * Tells whether the argument service is supported by the loader.<br/>
     * If returns <i>true</i>, then {@link #load(Class, ConfigurationReader, TriggerSerializer)} must return an instance
     * 
     * @param clazz
     * @return true if supported, false otherwize
     */
    public boolean isServiceSupported(Class<T> clazz);

    /**
     * Load a Snaperr service. 
     * 
     * @param clazz service to be loaded
     * @param configReader to be able to read configuration values
     * @param triggerSerializer Only necessary when the provided service depends on the serializer
     * @return service instance if {@link #isServiceSupported(Class)} returns <i>true</i>
     * otherwise this method must return <i>NULL</i>
     */
    public T load(Class<T> clazz, ConfigurationReader configReader, TriggerSerializer triggerSerializer);
    
    /*
     * Sorry, TriggerHandler depends on TriggerSerializer because sometimes we just want to log out
     * the serialized text with Slf4jHandler, sometimes with Moroi, but I did not want to make the
     * loading logic even more complicated, so a service should use it when it needs otherwise
     * don't care about it
     */
}
