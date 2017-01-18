package io.ghostwriter.rt.snaperr.handler;

import io.ghostwriter.rt.snaperr.SnaperrTracer;
import io.ghostwriter.rt.snaperr.serializer.StringSerializer;
import io.ghostwriter.rt.snaperr.serializer.TriggerSerializer;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jHandler implements TriggerHandler {

    private static Logger LOG = LoggerFactory.getLogger(Slf4jHandler.class);

    private TriggerSerializer<String> serializer = new StringSerializer();

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
