package io.ghostwriter.rt.snaperr.moroi.serializer;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.api.SnaperrServiceLoader;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

public class MoroiSerializerServiceLoader implements SnaperrServiceLoader {

    @Override
    public <T> T load(Class<T> clazz, ConfigurationReader configReader) {
	
	if (isServiceSupported(clazz)) {
	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);
	    final String moroiAppUUID = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_APP_UUID);
	    final TriggerSerializer<String> result = new MoroiSerializer(moroiAppUUID);
	    return (T) result;
	}
	else {
	    return null;
	}

    }

    @Override
    public boolean isServiceSupported(Class<?> clazz) {
	return TriggerSerializer.class.equals(clazz);
    }

}
