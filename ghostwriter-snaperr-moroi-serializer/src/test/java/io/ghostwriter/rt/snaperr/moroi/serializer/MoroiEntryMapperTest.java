package io.ghostwriter.rt.snaperr.moroi.serializer;

import io.ghostwriter.rt.snaperr.ErrorTriggerImpl;
import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MoroiEntryMapperTest {

    @Test
    public void testMultipleScopes() {
        final String appUUID = "appName";

        Throwable throwable = new IllegalArgumentException("someError");
        ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
        referenceTracker.pushScope(this, "method1");
        referenceTracker.track("var1", 1);
        referenceTracker.track("var2", 2);
        referenceTracker.pushScope(this, "method2");
        referenceTracker.track("var1", 1);
        referenceTracker.track("var2", 2);
        referenceTracker.pushScope(this, "method3");
        referenceTracker.track("var1", 1);
        referenceTracker.track("var2", 2);

        ErrorTrigger errorTrigger = new ErrorTriggerImpl(referenceTracker, throwable);

        MoroiEntryMapper mapper = new MoroiEntryMapper(appUUID);
        MoroiEntry moroiEntry = mapper.toMoroiEntry(errorTrigger);

        List<ContextData> contextDataList = moroiEntry.getData().getAttributes().getContext().getData();

        Assert.assertEquals("Check expected number of contexts in MoroiEntry", 3, contextDataList.size());
        Assert.assertEquals("scope order assertation 1st element", "method3", contextDataList.get(0).getMethod());
        Assert.assertEquals("scope order assertation 2nd element", "method2", contextDataList.get(1).getMethod());
        Assert.assertEquals("scope order assertation 3rd element", "method1", contextDataList.get(2).getMethod());

	/*
	 * it's reverse order just like a stack trace
	 */
    }

}
