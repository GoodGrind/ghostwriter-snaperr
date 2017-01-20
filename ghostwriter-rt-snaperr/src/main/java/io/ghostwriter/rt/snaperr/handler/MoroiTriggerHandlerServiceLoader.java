package io.ghostwriter.rt.snaperr.handler;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.serializer.MoroiSerializer;
import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;

/**
 * Load the {@link MoroiHandler}
 *
 */
public class MoroiTriggerHandlerServiceLoader implements SnaperrServiceLoader<TriggerHandler> {

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

	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);
	    
	    final String moroiErrorUrl = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_ERROR_URL);
	    final boolean noSslHostnameVerification = Boolean.parseBoolean(
		    gwProperties.getProperty(ConfigurationReader.CFG_MOROI_NO_HOSTNAME_VERIFICATION, "false"));
	    
	    final TriggerHandler triggerHandler = new MoroiHandler(moroiErrorUrl, triggerSerializer,
		    noSslHostnameVerification);
	    
	    return triggerHandler;

	}
	else {
	    return null;
	}
    }

}
