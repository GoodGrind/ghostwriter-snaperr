package io.ghostwriter.rt.snaperr.tracker;


import java.util.Map;

final public class TrackedScopeImpl implements TrackedScope {

    private Object source;

    private String methodName;

    private Map<String, TrackedValue> references;

    public TrackedScopeImpl(Object source, String methodName, Map<String, TrackedValue> references) {
        this.source = source;
        this.methodName = methodName;
        this.references = references;
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Map<String, TrackedValue> getReferences() {
        return references;
    }

    public void setReferences(Map<String, TrackedValue> references) {
        this.references = references;
    }
}
