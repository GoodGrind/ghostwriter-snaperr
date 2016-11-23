package io.ghostwriter.rt.snaperr;

import io.ghostwriter.Tracer;
import io.ghostwriter.rt.snaperr.handler.Slf4jHandler;
import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;

import java.util.Objects;

public class GhostWriterSnaperr implements Tracer {

    static final long DEFAULT_ERROR_WINDOW_SIZE_MS = 1000;
    static final int DEFAULT_MAX_ERROR_COUNT_IN_WINDOW = 5;

    private final ReferenceTracker referenceTracker;

    private final ThreadLocal<ErrorState> errorTrackerThreadLocal = new ThreadLocal<ErrorState>() {

        @Override
        protected ErrorState initialValue() {
            return new ErrorState();
        }
    };
    /**
     * For throttling, amount of time where maximum {@link #maxErrorsInWindow}
     * errors are allowed. < 0 means no throttling
     */
    private final long errorWindowSize;
    /**
     * For throttling, amount of errors allowed in each throttling window.
     * < 0 means no throttling
     */
    private final int maxErrorsInWindow;
    /* TODO (pal): Shouldn't it be final?
     * This object will be shared across threads.
     *
     * Although this field is effectively immutable during runtime
     */
    private TriggerHandler triggerHandler;

    public GhostWriterSnaperr() {
        this(defaultReferenceTracker(), defaultTriggerHandler(), DEFAULT_ERROR_WINDOW_SIZE_MS,
                DEFAULT_MAX_ERROR_COUNT_IN_WINDOW);
    }

    public GhostWriterSnaperr(TriggerHandler triggerHandler) {
        this(defaultReferenceTracker(), triggerHandler, DEFAULT_ERROR_WINDOW_SIZE_MS,
                DEFAULT_MAX_ERROR_COUNT_IN_WINDOW);
    }

    public GhostWriterSnaperr(ReferenceTracker referenceTracker, TriggerHandler triggerHandler,
                              long errorWindowSize, int maxErrorsInWindow) {
        this.referenceTracker = Objects.requireNonNull(referenceTracker);
        this.triggerHandler = triggerHandler;
        this.errorWindowSize = errorWindowSize;
        this.maxErrorsInWindow = maxErrorsInWindow;
    }

    private static TriggerHandler defaultTriggerHandler() {
        return new Slf4jHandler();
    }

    private static ReferenceTracker defaultReferenceTracker() {
        return new StackBasedReferenceTracker();
    }

    @Override
    public void entering(Object source, String method, Object... params) {
        if (hasPendingProcessing()) {
            return;
        }

        referenceTracker.pushScope(source, method);

        for (int i = 0; i < params.length - 1; i++) {
            final Object paramName = params[i++];
            final Object paramValue = params[i];
            final String name = (String) paramName;
            referenceTracker.track(name, paramValue);
        }
    }

    @Override
    public void exiting(Object source, String method) {
        if (hasPendingProcessing()) {
            return;
        }

        referenceTracker.popScope();
    }

    @Override
    public void valueChange(Object source, String method, String variable, Object value) {
        if (hasPendingProcessing()) {
            return;
        }

        referenceTracker.track(variable, value);
    }

    @Override
    public <T> void returning(Object source, String method, T returnValue) {
        // No need to do anything here, 'exiting' is called after returning and it takes care of cleanup.
        // We don't capture a result of a method here.
    }

    @Override
    public void onError(Object source, String method, Throwable error) {
        if (hasPendingProcessing() || isPropagatingException(error) || throttleControl()) {
            return;
        }

        // The trigger handler executes in the same thread as the one that just "crashed".
        // This way we can guarantee that the watched references are not changed. At least not by the current thread.
        // It is possible that the snapshot contains a reference to a global state that could be modified by other threads.
        // However that can _only_ (I'm 80% sure!) happen when the original code already has race conditions.
        
        /* The missing 20%: It's valid to use and therefore GW tracks concurrent collections, atomic references, volatile
         * variables. They don't imply race conditions but they can be changed by other threads in the meantime.
         * The error can also happen outside of the 'critical section' when the variables are not meant to be used by the
         * application yet, but we will read them.
         */

        ErrorTrigger trigger = new ErrorTrigger(referenceTracker, error);

        startTriggerProcessing(trigger);
        triggerHandler.onError(trigger);
        stopTriggerProcessing();
    }

    private boolean isPropagatingException(Throwable error) {
        final ErrorTrigger processedErrorTrigger = getProcessedErrorTrigger();
        if (processedErrorTrigger == null) {
            return false;
        }

        final Throwable processedThrowable = processedErrorTrigger.getThrowable();
        return processedThrowable.equals(error);
    }

    @Override
    public void timeout(Object source, String method, long timeoutThreshold, long timeout) {
        if (hasPendingProcessing()) {
            return;
        }
        // Unlike in the case of on error trigger, we don't need to guard for timeouts propagating.
        // This is an opt-in feature, so if the user annotates the call-chain, then we might end up raising timeouts at all steps...

        TimeoutTrigger trigger = new TimeoutTrigger(referenceTracker, timeoutThreshold, timeout);
        startTriggerProcessing(null);
        triggerHandler.onTimeout(trigger);
        stopTriggerProcessing();
    }


    private ErrorTrigger getProcessedErrorTrigger() {
        return errorTrackerThreadLocal.get().getProcessedErrorTrigger();
    }

    /**
     * Only {@link ErrorState#getMaxErrorCountInBucket()} number of errors
     * are handled within a {@link ErrorState#getPurgeBucketMs()} window.
     * <p>
     * <p>if the maxErrorCountInBucket number of errors have been reached,
     * then there are no new errors handled until the bucket is emptied.
     *
     * @return <li> true - throttling, must not handle error
     * <li> false - no throttling, error the handle now
     */
    private boolean throttleControl() {
        if (isThrottlingDisabled()) {
            return false;
        }

        final long errorWindowSizeMs = getErrorWindowSizeMs();
        final int maxErrorCountInWindow = getMaxErrorCountInWindow();
        ErrorState errorState = errorTrackerThreadLocal.get();

        final long nowMs = System.currentTimeMillis();
        final long elapsedSinceLastPurgeMs = nowMs - errorState.getBucketPurgedLastTimeMs();

        if (elapsedSinceLastPurgeMs >= errorWindowSizeMs) {
            // Empty the bucket
            errorState.setErrorsInBucket(0);
            errorState.setBucketPurgedLastTimeMs(nowMs);
        }

        int errorsInBucket = errorState.getErrorsInBucket();
        errorsInBucket++;
        errorState.setErrorsInBucket(errorsInBucket);

        return errorsInBucket > maxErrorCountInWindow;
    }

    private boolean isThrottlingDisabled() {
        final long errorWindowSizeMs = getErrorWindowSizeMs();
        final int maxErrorCountInWindow = getMaxErrorCountInWindow();

        return errorWindowSizeMs < 1L || maxErrorCountInWindow < 1;
    }

    private void startTriggerProcessing(ErrorTrigger errorTrigger) {
        ErrorState errorState = errorTrackerThreadLocal.get();
        errorState.setProcessingInProgress(true);
        errorState.setProcessedErrorTrigger(errorTrigger);
    }

    private void stopTriggerProcessing() {
        errorTrackerThreadLocal.get().setProcessingInProgress(false);
    }

    private boolean hasPendingProcessing() {
        return errorTrackerThreadLocal.get().isProcessingInProgress();
    }

    public void setTriggerHandler(TriggerHandler triggerHandler) {
        this.triggerHandler = Objects.requireNonNull(triggerHandler);
    }

    private long getErrorWindowSizeMs() {
        return errorWindowSize;
    }

    private int getMaxErrorCountInWindow() {
        return maxErrorsInWindow;
    }

    /**
     * To hold all invariants together
     */
    private static class ErrorState {

        /**
         * We store the triggered error, since we use it to avoid triggering additional error events on
         * the same thread.
         */
        private ErrorTrigger processedErrorTrigger;

        /**
         * When we are currently in error handling state, then we should not
         * accidentally handle another error
         */
        private boolean processingInProgress = false;

        private int errorsInBucket = 0;
        private long bucketPurgedLastTimeMs = System.currentTimeMillis();

        ErrorTrigger getProcessedErrorTrigger() {
            return processedErrorTrigger;
        }

        void setProcessedErrorTrigger(ErrorTrigger processedErrorTrigger) {
            this.processedErrorTrigger = processedErrorTrigger;
        }

        boolean isProcessingInProgress() {
            return processingInProgress;
        }

        void setProcessingInProgress(boolean processingInProgress) {
            this.processingInProgress = processingInProgress;
        }

        int getErrorsInBucket() {
            return errorsInBucket;
        }

        void setErrorsInBucket(int errorsInBucket) {
            this.errorsInBucket = errorsInBucket;
        }

        long getBucketPurgedLastTimeMs() {
            return bucketPurgedLastTimeMs;
        }

        void setBucketPurgedLastTimeMs(long bucketPurgedLastTimeMs) {
            this.bucketPurgedLastTimeMs = bucketPurgedLastTimeMs;
        }

    }
}
