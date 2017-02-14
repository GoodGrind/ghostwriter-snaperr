package io.ghostwriter.rt.snaperr.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ghostwriter.rt.snaperr.SnaperrTracer;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;

public class Slf4jHandler implements TriggerHandler<String> {

    private static Logger LOG = LoggerFactory.getLogger(Slf4jHandler.class);
    
    @Override
    public void onError(String serializedError) {
        StringBuilder sb = new StringBuilder();
        sb.append(SnaperrTracer.class.getCanonicalName()).append(" state snapshot on error: \n");
        sb.append(serializedError);

        LOG.error(sb.toString());
    }

    @Override
    public void onTimeout(String serializedTimeout) {
        StringBuilder sb = new StringBuilder();
        sb.append(SnaperrTracer.class.getCanonicalName()).append(" state snapshot on timeout: \n");
        sb.append(serializedTimeout);

        LOG.error(sb.toString());
    }

}
