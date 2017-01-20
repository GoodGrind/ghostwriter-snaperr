package io.ghostwriter.rt.snaperr.api;

import java.util.Iterator;

/**
 * Holds information about the program state at the point of error:
 * <li>Throwable
 * <li>List of Scopes on the stack
 */
public interface ErrorTrigger {

    public Throwable getThrowable();

    TrackedScope currentScope();

    Iterator<TrackedScope> getTrackedScopeIterator();
}
