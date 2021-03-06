package io.ghostwriter.rt.snaperr.json.serializer;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

/**
 * Default implementation of SnaperrServiceLoader returns:
 * <li>{@link JsonSerializer}
 * 
 * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader
 */
public class JsonSnaperrServiceLoader implements SnaperrServiceLoader {

    /**
     * @see io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader#load(java.lang.Class, io.ghostwriter.rt.snaperr.ConfigurationReader)
     * @return
     * <li>{@link JsonSerializer}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(Class<T> clazz, ConfigurationReader configReader) {
	
	if (TriggerSerializer.class.equals(clazz)) {
	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);
	    final String moroiAppUUID = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_APP_UUID);
	    final TriggerSerializer<String> result = new JsonSerializer(moroiAppUUID);
	    return (T) result;
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
	return TriggerSerializer.class.equals(clazz);
    }

}
