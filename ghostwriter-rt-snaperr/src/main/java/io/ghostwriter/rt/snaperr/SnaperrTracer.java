package io.ghostwriter.rt.snaperr;

import java.util.Objects;

import io.ghostwriter.Tracer;
import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.ThrottleControlStrategy;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.api.TriggerHandler;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

public class SnaperrTracer implements Tracer {

    static final long DEFAULT_ERROR_WINDOW_SIZE_MS = 1000;
    static final int DEFAULT_MAX_ERROR_COUNT_IN_WINDOW = 5;

    private final ReferenceTracker referenceTracker;
    
    @SuppressWarnings("rawtypes")
    final private TriggerHandler triggerHandler;
    
    @SuppressWarnings("rawtypes")
    final private TriggerSerializer triggerSerializer;


    private final ThreadLocal<ErrorState> errorTrackerThreadLocal = new ThreadLocal<ErrorState>() {

	@Override
	protected ErrorState initialValue() {
	    return new ErrorState();
	}
    };
    
    private final ThrottleControlStrategy throttleControl;


    public SnaperrTracer(ReferenceTracker referenceTracker, TriggerSerializer<?> triggerSerializer,
	    TriggerHandler<?> triggerHandler, ThrottleControlStrategy throttleControl) {
	this.referenceTracker = Objects.requireNonNull(referenceTracker);
	this.triggerHandler = triggerHandler;
	this.throttleControl = throttleControl;
	this.triggerSerializer = triggerSerializer;
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
	// No need to do anything here, 'exiting' is called after returning and
	// it takes care of cleanup.
	// We don't capture a result of a method here.
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Object source, String method, Throwable error) {
	if (hasPendingProcessing() || isPropagatingException(error)) {
	    return;
	}

	// The trigger handler executes in the same thread as the one that just
	// "crashed".
	// This way we can guarantee that the watched references are not
	// changed. At least not by the current thread.
	// It is possible that the snapshot contains a reference to a global
	// state that could be modified by other threads.
	// However that can _only_ (I'm 80% sure!) happen when the original code
	// already has race conditions.

	/*
	 * The missing 20%: It's valid to use and therefore GW tracks concurrent
	 * collections, atomic references, volatile variables. They don't imply
	 * race conditions but they can be changed by other threads in the
	 * meantime. The error can also happen outside of the 'critical section'
	 * when the variables are not meant to be used by the application yet,
	 * but we will read them.
	 */

	ErrorTrigger trigger = new ErrorTriggerImpl(referenceTracker, error);
	
	synchronized (throttleControl) {
	    if (! throttleControl.isHandleError(trigger)) {
		return;
	    }	    
	}

	startTriggerProcessing(trigger);
	Object serializedError = triggerSerializer.serializeTrigger(trigger);
	triggerHandler.onError(serializedError);
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

    @SuppressWarnings("unchecked")
    @Override
    public void timeout(Object source, String method, long timeoutThreshold, long timeout) {
	if (hasPendingProcessing()) {
	    return;
	}
	// Unlike in the case of on error trigger, we don't need to guard for
	// timeouts propagating.
	// This is an opt-in feature, so if the user annotates the call-chain,
	// then we might end up raising timeouts at all steps...

	TimeoutTrigger trigger = new TimeoutTriggerImpl(referenceTracker, timeoutThreshold, timeout);
	
	synchronized (throttleControl) {
	    if (!throttleControl.isHandleTimeout(trigger)) {
		return;
	    }
	}
	
	startTriggerProcessing(null);
	Object serializedTimeout = triggerSerializer.serializeTrigger(trigger);
	triggerHandler.onTimeout(serializedTimeout);
	stopTriggerProcessing();
    }

    private ErrorTrigger getProcessedErrorTrigger() {
	return errorTrackerThreadLocal.get().getProcessedErrorTrigger();
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

    /**
     * To hold all invariants together
     */
    private static class ErrorState {

	/**
	 * We store the triggered error, since we use it to avoid triggering
	 * additional error events on the same thread.
	 */
	private ErrorTrigger processedErrorTrigger;

	/**
	 * When we are currently in error handling state, then we should not
	 * accidentally handle another error
	 */
	private boolean processingInProgress = false;

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

    }
}
