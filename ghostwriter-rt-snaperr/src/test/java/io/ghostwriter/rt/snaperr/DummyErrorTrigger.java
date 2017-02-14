package io.ghostwriter.rt.snaperr;

import java.util.Iterator;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.TrackedScope;

public class DummyErrorTrigger implements ErrorTrigger {

    @Override
    public Throwable getThrowable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TrackedScope currentScope() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Iterator<TrackedScope> getTrackedScopeIterator() {
	// TODO Auto-generated method stub
	return null;
    }

}
