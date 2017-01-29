package io.ghostwriter.rt.snaperr.handler;

import io.ghostwriter.rt.snaperr.api.TriggerHandler;

public class NoOpHandler implements TriggerHandler<String> {
    
    @Override
    public void onError(String serializedError) {
    }

    @Override
    public void onTimeout(String serializedTimeout) {
    }

}
