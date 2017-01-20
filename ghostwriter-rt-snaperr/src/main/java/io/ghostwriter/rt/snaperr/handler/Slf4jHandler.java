package io.ghostwriter.rt.snaperr.handler;

import io.ghostwriter.rt.snaperr.SnaperrTracer;
import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jHandler implements TriggerHandler {

    private static Logger LOG = LoggerFactory.getLogger(Slf4jHandler.class);

    private final TriggerSerializer<String> serializer;

    public Slf4jHandler(TriggerSerializer<String> serializer) {
	this.serializer = serializer;
    }
    
    @Override
    public void onError(ErrorTrigger errorTrigger) {
        StringBuilder sb = new StringBuilder();
        sb.append(SnaperrTracer.class.getCanonicalName()).append(" state snapshot: \n");
        final String str = serializer.serializeTrigger(errorTrigger);
        sb.append(str);

        LOG.error(sb.toString());
    }

    @Override
    public void onTimeout(TimeoutTrigger timeoutTrigger) {
        throw new UnsupportedOperationException();
    }

}
