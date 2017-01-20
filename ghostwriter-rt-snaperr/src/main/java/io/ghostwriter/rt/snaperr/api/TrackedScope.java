package io.ghostwriter.rt.snaperr.api;


import java.util.Map;

import io.ghostwriter.rt.snaperr.tracker.TrackedValue;

/**
 * Keeps info about one scope (i.e. one method call):
 * <li>source (class or instance)
 * <li>method name
 * <li>variables in the scope
 */
public interface TrackedScope {
    
    public Object getSource();

    public String getMethodName();

    public Map<String, TrackedValue> getReferences();
    
}
