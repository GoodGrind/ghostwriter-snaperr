package io.ghostwriter.rt.snaperr.moroi.serializer;

import io.ghostwriter.rt.snaperr.ErrorTriggerImpl;
import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static org.junit.Assert.assertNotNull;

import java.util.List;

public class MoroiEntryMapperTest {
    

    /**
     * We don not include line number, timestamp and exception serialization because
     * they differ at each run. (Line number can be broken easily too by chaning the test)
     */
    @Test
    public void testMoroiSerialization() {

        final boolean doPrettyPrint = false;
        final int stackTraceLimit = 1;
        final String appName = "appName";
        final MoroiSerializer jsonSerializer = new MoroiSerializer(appName, doPrettyPrint, stackTraceLimit);

        Throwable throwable = new IllegalArgumentException("someError");
        ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
        referenceTracker.pushScope(this, "testGeneralInformationSerialization");
        referenceTracker.track("doPrettyPrint", doPrettyPrint);
        referenceTracker.track("appName", appName);

        ErrorTrigger errorTrigger = new ErrorTriggerImpl(referenceTracker, throwable);

        final String json = jsonSerializer.serializeTrigger(errorTrigger);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.enableComplexMapKeySerialization();
        if (doPrettyPrint) {
            gsonBuilder.setPrettyPrinting();
        }

        gsonBuilder.registerTypeAdapter(Class.class, new ClassSerializer());
        gsonBuilder.registerTypeAdapter(Thread.class, new ThreadSerializer());
        gsonBuilder.registerTypeAdapterFactory(new ErrorHandlerTypeAdapterFactory());
        Gson gson = gsonBuilder.create();

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonObject jData = jsonObject.get("data").getAsJsonObject();
        assertNotNull("type does not exist in the json", jData.get("type"));
        assertNotNull("attributes does not exist in the json", jData.get("attributes"));
        
        JsonObject jAttributes = jData.get("attributes").getAsJsonObject();
        assertNotNull("application_uuid does not exist in the json", jAttributes.get("application_uuid"));
        assertNotNull("file does not exist in the json", jAttributes.get("file"));
        assertNotNull("original_timestamp does not exist in the json", jAttributes.get("original_timestamp"));
        assertNotNull("exception does not exist in the json", jAttributes.get("exception"));
        assertNotNull("stacktrace does not exist in the json", jAttributes.get("stacktrace"));
        assertNotNull("context does not exist in the json", jAttributes.get("context"));
        assertNotNull("instance does not exist in the json", jAttributes.get("instance"));
        assertNotNull("line does not exist in the json", jAttributes.get("line"));
        
        System.out.println(json);
        
        JsonObject jContext = jAttributes.get("context").getAsJsonObject();
	JsonArray jData2 = jContext.get("data").getAsJsonArray();
        assertNotNull("class does not exist in the json", jData2.get(0).getAsJsonObject().get("class"));
        assertNotNull("method does not exist in the json", jData2.get(0).getAsJsonObject().get("method"));
        assertNotNull("variables does not exist in the json", jData2.get(0).getAsJsonObject().get("variables"));
    }

    @Test
    public void testMultipleScopeInEntrySerialization() {

        final boolean doPrettyPrint = false;
        final int stackTraceLimit = 1;
        final String appName = "appName";
        final MoroiSerializer jsonSerializer = new MoroiSerializer(appName, doPrettyPrint, stackTraceLimit);

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

        final String json = jsonSerializer.serializeTrigger(errorTrigger);

        Assert.assertTrue("Find 1st context in Json", json.contains("\"method\":\"method1\""));
        Assert.assertTrue("Find 2nd context in Json", json.contains("\"method\":\"method2\""));
        Assert.assertTrue("Find 3rd context in Json", json.contains("\"method\":\"method3\""));
    }


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
