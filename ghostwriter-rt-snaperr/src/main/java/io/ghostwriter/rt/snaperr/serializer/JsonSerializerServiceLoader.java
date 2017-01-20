package io.ghostwriter.rt.snaperr.serializer;

import java.util.Properties;

import io.ghostwriter.rt.snaperr.ConfigurationReader;
import io.ghostwriter.rt.snaperr.SnaperrServiceLoader;

public class JsonSerializerServiceLoader implements SnaperrServiceLoader<TriggerSerializer> {

    @Override
    public TriggerSerializer load(Class<TriggerSerializer> clazz, ConfigurationReader configReader,
	    TriggerSerializer triggerSerializer) {

	if (triggerSerializer != null) {
	    throw new IllegalStateException("An implementation (" + triggerSerializer.getClass().getName() + ") of "
		    + TriggerSerializer.class + " is already loaded. Revise the Snaperr service loading order");
	}

	if (isServiceSupported(clazz)) {
	    final String gwAppName = configReader.getGwAppName();
	    final Properties gwProperties = configReader.getGwProperties(gwAppName);
	    final String moroiAppUUID = gwProperties.getProperty(ConfigurationReader.CFG_MOROI_APP_UUID);
	    final TriggerSerializer<String> result = new MoroiSerializer(moroiAppUUID);
	    return triggerSerializer;
	}
	else {
	    return null;
	}

    }

    @Override
    public boolean isServiceSupported(Class<TriggerSerializer> clazz) {
	return TriggerSerializer.class.equals(clazz);
    }

}
