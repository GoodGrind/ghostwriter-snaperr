package io.ghostwriter.rt.snaperr.tracker;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ThrottleControlStrategy;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;

public class DefaultThrottleControlStrategy implements ThrottleControlStrategy {

    /**
     * For throttling, amount of time where maximum {@link #maxErrorsInWindow}
     * errors are allowed. < 0 means no throttling
     */
    private final long errorWindowSizeMs;
    /**
     * For throttling, amount of errors allowed in each throttling window. < 0
     * means no throttling
     */
    private final int maxErrorsInWindow;

    private int errorsInBucket = 0;
    private long bucketPurgedLastTimeMs = getCurrentTimeMilis();

    /**
     * @param errorWindowSizeMs Window size in Milisec
     * @param maxErrorsInWindow maximum number of errors in the window to be handled
     */
    public DefaultThrottleControlStrategy(long errorWindowSizeMs, int maxErrorsInWindow) {
	this.errorWindowSizeMs = errorWindowSizeMs;
	this.maxErrorsInWindow = maxErrorsInWindow;
    }

    @Override
    public boolean isHandleTimeout(TimeoutTrigger timeoutTrigger) {
	return ! throttleControl();
    }

    @Override
    public boolean isHandleError(ErrorTrigger errorTrigger) {
	return ! throttleControl();
    }

    /**
     * Only {@link ErrorState#getMaxErrorCountInBucket()} number of errors are
     * handled within a {@link ErrorState#getPurgeBucketMs()} window.
     * <p>
     * <p>
     * if the maxErrorCountInBucket number of errors have been reached, then
     * there are no new errors handled until the bucket is emptied.
     *
     * @return
     *         <li>true - throttling, must not handle error
     *         <li>false - no throttling, error the handle now
     */
    private boolean throttleControl() {
	if (isThrottlingDisabled()) {
	    return false;
	}

	final long errorWindowSizeMs = getErrorWindowSizeMs();
	final int maxErrorCountInWindow = getMaxErrorCountInWindow();
	
	final long nowMs = getCurrentTimeMilis();
	final long elapsedSinceLastPurgeMs = nowMs - bucketPurgedLastTimeMs;

	if (elapsedSinceLastPurgeMs >= errorWindowSizeMs) {
	    // Empty the bucket
	    errorsInBucket = 0;
	    bucketPurgedLastTimeMs = nowMs;
	}

	errorsInBucket++;

	return errorsInBucket > maxErrorCountInWindow;
    }

    private boolean isThrottlingDisabled() {
	final long errorWindowSizeMs = getErrorWindowSizeMs();
	final int maxErrorCountInWindow = getMaxErrorCountInWindow();

	return errorWindowSizeMs < 1L || maxErrorCountInWindow < 1;
    }

    private long getErrorWindowSizeMs() {
	return errorWindowSizeMs;
    }

    private int getMaxErrorCountInWindow() {
	return maxErrorsInWindow;
    }
    
    /**
     * @return current time in miliseconds
     */
    protected long getCurrentTimeMilis() {
	return System.currentTimeMillis();
    }

}
