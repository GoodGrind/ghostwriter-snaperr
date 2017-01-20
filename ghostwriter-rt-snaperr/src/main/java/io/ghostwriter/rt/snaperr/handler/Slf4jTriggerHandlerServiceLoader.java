package io.ghostwriter.rt.snaperr.handler;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;

/**
 * Load the {@link Slf4jHandler}
 *
 */
public class Slf4jTriggerHandlerServiceLoader implements SnaperrServiceLoader<TriggerHandler> {

    @Override
    public boolean isServiceSupported(Class<TriggerHandler> clazz) {
	return TriggerHandler.class.equals(clazz);
    }

    @Override
    public TriggerHandler load(Class<TriggerHandler> clazz, ConfigurationReader configReader,
	    TriggerSerializer triggerSerializer) {

	if (isServiceSupported(clazz)) {

	    if (triggerSerializer == null) {
		throw new IllegalStateException(this.getClass().getName() + " requires a loaded implementation of "
			+ TriggerSerializer.class.getName() + "");
	    }

	    final TriggerHandler triggerHandler = new Slf4jHandler(triggerSerializer);

	    return triggerHandler;

	}
	else {
	    return null;
	}
    }

}
