package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.TrackedScope;

import java.util.Iterator;
import java.util.Objects;

final public class ErrorTriggerImpl implements ErrorTrigger {

    private final ReferenceTracker referenceTracker;

    private final Throwable throwable;

    public ErrorTriggerImpl(ReferenceTracker referenceTracker, Throwable throwable) {
	this.referenceTracker = Objects.requireNonNull(referenceTracker);
	this.throwable = Objects.requireNonNull(throwable);
    }

    @Override
    public Throwable getThrowable() {
	return throwable;
    }

    @Override
    public TrackedScope currentScope() {
	return referenceTracker.currentScope();
    }

    @Override
    public Iterator<TrackedScope> getTrackedScopeIterator() {
	return referenceTracker.getTrackedScopeIterator();
    }
}
