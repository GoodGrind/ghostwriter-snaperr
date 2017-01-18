package io.ghostwriter.rt.snaperr.tracker;


import java.util.Map;

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
