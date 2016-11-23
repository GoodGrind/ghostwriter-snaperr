package io.ghostwriter.rt.snaperr.trigger;


import io.ghostwriter.rt.snaperr.tracker.TrackedValue;

import java.util.Map;


final public class StateSnapshot {

    private final Object context;

    private final String methodName;

    private final Map<String, TrackedValue> watched;

    public StateSnapshot(Object context, String methodName, Map<String, TrackedValue> watched) {
        this.context = context;
        this.methodName = methodName;
        this.watched = watched;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object getContext() {
        return context;
    }

    public Map<String, TrackedValue> getWatched() {
        return watched;
    }
}
