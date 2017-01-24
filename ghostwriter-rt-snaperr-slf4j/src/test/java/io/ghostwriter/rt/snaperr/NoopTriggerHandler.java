package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.api.TriggerHandler;

/**
 * Do nothing
 * @author pal
 *
 */
public class NoopTriggerHandler implements TriggerHandler<String> {

    @Override
    public void onError(String serializedError) {
	
    }

    @Override
    public void onTimeout(String serializedTimeout) {
	
    }

}
