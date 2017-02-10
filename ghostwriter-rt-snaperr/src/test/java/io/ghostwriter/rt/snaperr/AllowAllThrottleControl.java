package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ThrottleControlStrategy;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;

/**
 * Default to handle triggers
 * @author pal
 *
 */
public class AllowAllThrottleControl implements ThrottleControlStrategy {

    @Override
    public boolean isHandleTimeout(TimeoutTrigger timeoutTrigger) {
	return true;
    }

    @Override
    public boolean isHandleError(ErrorTrigger errorTrigger) {
	return true;
    }

}
