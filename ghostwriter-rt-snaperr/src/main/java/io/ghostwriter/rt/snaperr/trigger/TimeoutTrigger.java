package io.ghostwriter.rt.snaperr.trigger;


import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;

import java.util.Objects;


public interface TimeoutTrigger {

    public ReferenceTracker getReferenceTracker();

    public long getTimeoutThreshold();

    public long getTimeout();
}
