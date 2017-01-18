package io.ghostwriter.rt.snaperr.tracker;


import java.util.*;

public class StackBasedReferenceTracker implements ReferenceTracker {

    private ThreadLocal<ScopeStack> trackedThreadStack = new ThreadLocal<ScopeStack>() {
        @Override
        protected ScopeStack initialValue() {
            ScopeStack trackedScopeStack = new ScopeStack();
            return trackedScopeStack;
        }
    };

    @Override
    public <T> void track(String variableName, T variableReference) {
        final ScopeStack trackedScopes = trackedThreadStack.get();
        final TrackedScope currentScope = trackedScopes.peek();
        
        /* TODO (pal):
         * Consider changing Map<K,V> to Set<V> and TrackValue.hashCode() only using variable name:
         * - we anyways only store the last value of a variable
         * - benefits:
         * -- less equals()-hashCode() calls
         * -- less reference storage (only store reference to the TrackedValue object but not the key)
         */

        final Map<String, TrackedValue> references = currentScope.getReferences();
        TrackedValue savedTrackedValue = references.get(variableName);

        if (savedTrackedValue == null) {
            TrackedValue trackedValue = new TrackedValue(Objects.requireNonNull(variableName), variableReference);
            references.put(variableName, trackedValue);
        } else {
            savedTrackedValue.setValue(variableReference);
        }

    }

    @Override
    public void pushScope(Object source, String methodName) {
        final ScopeStack trackedScopes = trackedThreadStack.get();
        trackedScopes.push(source, methodName, new HashMap<String, TrackedValue>());
    }

    @Override
    public void popScope() {
        final ScopeStack trackedScopes = trackedThreadStack.get();
        trackedScopes.pop();
    }

    @Override
    public TrackedScope currentScope() {
        final ScopeStack trackedScopes = trackedThreadStack.get();
        final TrackedScope currentScope = trackedScopes.peek();
        Map<String, TrackedValue> lockedReferences = Collections.unmodifiableMap(currentScope.getReferences());
        return new TrackedScopeImpl(currentScope.getSource(), currentScope.getMethodName(), lockedReferences);
    }

    @Override
    public boolean isEmpty() {
        final ScopeStack trackedScopes = trackedThreadStack.get();
        return trackedScopes.isEmpty();
    }

    /**
     * Will return a reverse order iterator of a copy list
     * therefore remove() operation is unsupported
     */
    @Override
    public Iterator<TrackedScope> getTrackedScopeIterator() {
        return trackedThreadStack.get().iterator();
    }
}
