package io.ghostwriter.rt.snaperr.tracker;


import java.util.Iterator;
import java.util.Objects;

import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.api.TrackedScope;


public class ReferenceTrackerTraverser implements Iterator<TrackedScope> {

    private final ReferenceTracker referenceTracker;

    public ReferenceTrackerTraverser(ReferenceTracker referenceTracker) {
        this.referenceTracker = Objects.requireNonNull(referenceTracker);
    }

    @Override
    public boolean hasNext() {
        return !referenceTracker.isEmpty();
    }

    @Override
    public TrackedScope next() {
        final TrackedScope currentScope = referenceTracker.currentScope();
        referenceTracker.popScope();
        return currentScope;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
