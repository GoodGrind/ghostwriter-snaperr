package io.ghostwriter.rt.snaperr.slf4j.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ghostwriter.rt.snaperr.SnaperrTracer;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;

public class Slf4jHandler implements TriggerHandler<String> {

    private static Logger LOG = LoggerFactory.getLogger(Slf4jHandler.class);
    
    @Override
    public void onError(String serializedError) {
        StringBuilder sb = new StringBuilder();
        sb.append(SnaperrTracer.class.getCanonicalName()).append(" state snapshot: \n");
        sb.append(serializedError);

        LOG.error(sb.toString());
    }

    @Override
    public void onTimeout(String serializedTimeout) {
        throw new UnsupportedOperationException();
    }

}
