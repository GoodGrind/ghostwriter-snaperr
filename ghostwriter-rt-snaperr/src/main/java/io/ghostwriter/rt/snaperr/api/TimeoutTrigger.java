package io.ghostwriter.rt.snaperr.api;



public interface TimeoutTrigger {

    public ReferenceTracker getReferenceTracker();

    public long getTimeoutThreshold();

    public long getTimeout();
}
