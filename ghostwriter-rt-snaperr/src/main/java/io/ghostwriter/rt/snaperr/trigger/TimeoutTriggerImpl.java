package io.ghostwriter.rt.snaperr.trigger;


import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;

import java.util.Objects;


final public class TimeoutTriggerImpl implements TimeoutTrigger {

    private final ReferenceTracker referenceTracker;

    private final long timeoutThreshold;

    private final long timeout;

    public TimeoutTriggerImpl(ReferenceTracker referenceTracker, long timeoutThreshold, long timeout) {
        this.referenceTracker = Objects.requireNonNull(referenceTracker);
        this.timeoutThreshold = timeoutThreshold;
        this.timeout = timeout;
    }

    public ReferenceTracker getReferenceTracker() {
        return referenceTracker;
    }

    public long getTimeoutThreshold() {
        return timeoutThreshold;
    }

    public long getTimeout() {
        return timeout;
    }
}
