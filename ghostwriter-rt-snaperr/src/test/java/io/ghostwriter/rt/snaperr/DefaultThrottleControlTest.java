package io.ghostwriter.rt.snaperr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.tracker.DefaultThrottleControlStrategy;

public class DefaultThrottleControlTest {

    @Test
    public void testDefaultThrottleControlStrategy() {
	long errorWindowSize = 1000;
	int maxErrorsInWindow = 3;
	final long [] currentTimeMilis = {0L};
	DefaultThrottleControlStrategy throttleControl = new DefaultThrottleControlStrategy(errorWindowSize, maxErrorsInWindow) {
	    
	    @Override
	    protected long getCurrentTimeMilis() {
		return currentTimeMilis[0];
	    }
	};
	
	final ErrorTrigger dummyErrorTrigger = new DummyErrorTrigger();
	
	currentTimeMilis[0] = 10;
	boolean handleError = throttleControl.isHandleError(dummyErrorTrigger);
	assertTrue("Throttle control should allow handling error at time " + currentTimeMilis[0], handleError);

	currentTimeMilis[0] = 20;
	handleError = throttleControl.isHandleError(dummyErrorTrigger);
	assertTrue("Throttle control should allow handling error at time " + currentTimeMilis[0], handleError);

	currentTimeMilis[0] = 30;
	handleError = throttleControl.isHandleError(dummyErrorTrigger);
	assertTrue("Throttle control should allow handling error at time " + currentTimeMilis[0], handleError);

	currentTimeMilis[0] = 40;
	// This is where we reached maxErrorsInWindow = 3 within errorWindowSize = 1000
	handleError = throttleControl.isHandleError(dummyErrorTrigger);
	assertFalse("Throttle control should prohibit handling error at time " + currentTimeMilis[0], handleError);
	
	// now let's move forward in time and see if we can handle errors again
	currentTimeMilis[0] = 1000;
	handleError = throttleControl.isHandleError(dummyErrorTrigger);
	assertTrue("Throttle control should allow handling error at time " + currentTimeMilis[0], handleError);
    }
}
