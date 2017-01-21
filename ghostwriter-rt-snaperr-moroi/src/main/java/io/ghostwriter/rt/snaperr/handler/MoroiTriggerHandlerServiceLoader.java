package io.ghostwriter.rt.snaperr.handler;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;

/**
 * Load the {@link MoroiHandler}
 *
 */
public class MoroiTriggerHandlerServiceLoader implements SnaperrServiceLoader {

    @Override
    public boolean isServiceSupported(Class<?> clazz) {
	return TriggerHandler.class.equals(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T load(Class<T> clazz, ConfigurationReader configReader) {

	if (isServiceSupported(clazz)) {
	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);

	    final String moroiErrorUrl = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_ERROR_URL);
	    final boolean noSslHostnameVerification = Boolean.parseBoolean(
		    gwProperties.getProperty(ConfigurationReader.CFG_MOROI_NO_HOSTNAME_VERIFICATION, "false"));

	    @SuppressWarnings("rawtypes")
	    final TriggerHandler triggerHandler = new MoroiHandler(moroiErrorUrl, noSslHostnameVerification);

	    return (T) triggerHandler;

	}
	else {
	    return null;
	}
    }

}
