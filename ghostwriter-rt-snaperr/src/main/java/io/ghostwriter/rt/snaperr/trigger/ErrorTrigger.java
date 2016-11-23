package io.ghostwriter.rt.snaperr.trigger;

import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;

import java.util.Objects;


final public class ErrorTrigger {

    private final ReferenceTracker referenceTracker;

    private final Throwable throwable;

    public ErrorTrigger(ReferenceTracker referenceTracker, Throwable throwable) {
        this.referenceTracker = Objects.requireNonNull(referenceTracker);
        this.throwable = Objects.requireNonNull(throwable);
    }

    public ReferenceTracker getReferenceTracker() {
        return referenceTracker;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
