package io.ghostwriter.rt.snaperr.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import org.junit.Assert;
import org.junit.Test;

public class JsonSerializerTest {

    /**
     * We don not include line number, timestamp and exception serialization because
     * they differ at each run. (Line number can be broken easily too by chaning the test)
     */
    @Test
    public void testMoroiEntrySerialization() {
        final String expected1 = "{\"data\":{\"type\":\"exception\",\"attributes\":{\"application_uuid\":\"appName\","
                + "\"file\":\"JsonSerializerTest.java\",";
        final String expected2 = "\"context\":{\"data\":[{\"class\":\"io.ghostwriter.rt.snaperr.serializer.JsonSerializerTest\","
                + "\"method\":\"testGeneralInformationSerialization\","
                + "\"variables\":[{\"name\":\"appName\",\"value\":\"appName\",\"type\":\"java.lang.String\"},"
                + "{\"name\":\"doPrettyPrint\",\"value\":\"false\",\"type\":\"java.lang.Boolean\"}]}]},"
                + "\"instance\":\"io.ghostwriter.rt.snaperr.serializer.JsonSerializerTest\",\"line\":";

        final boolean doPrettyPrint = false;
        final int stackTraceLimit = 1;
        final String appName = "appName";
        final JsonSerializer jsonSerializer = new JsonSerializer(appName, doPrettyPrint, stackTraceLimit);

        Throwable throwable = new IllegalArgumentException("someError");
        ReferenceTracker referenceTracker = new StackBasedReferenceTracker();
        referenceTracker.pushScope(this, "testGeneralInformationSerialization");
        referenceTracker.track("doPrettyPrint", doPrettyPrint);
        referenceTracker.track("appName", appName);

        ErrorTrigger errorTrigger = new ErrorTrigger(referenceTracker, throwable);

        final String json = jsonSerializer.serializeTrigger(errorTrigger);

        Assert.assertTrue("Find expected output part 1", json.contains(expected1));
        Assert.assertTrue("Find expected output parrt 2", json.contains(expected2));
    }

    @Test
    public void testMultipleScopeInMoroiEntrySerialization() {

        final boolean doPrettyPrint = false;
        final int stackTraceLimit = 1;
        final String appName = "appName";
        final JsonSerializer jsonSerializer = new JsonSerializer(appName, doPrettyPrint, stackTraceLimit);

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

        ErrorTrigger errorTrigger = new ErrorTrigger(referenceTracker, throwable);

        final String json = jsonSerializer.serializeTrigger(errorTrigger);

        Assert.assertTrue("Find 1st context in Json", json.contains("\"method\":\"method1\""));
        Assert.assertTrue("Find 2nd context in Json", json.contains("\"method\":\"method2\""));
        Assert.assertTrue("Find 3rd context in Json", json.contains("\"method\":\"method3\""));
    }

    /**
     * It's a smoke test, should not throw exception
     */
    @Test
    public void testThreadClassGsonSerialization() {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Class.class, new ClassSerializer());
        gsonBuilder.registerTypeAdapter(Thread.class, new ThreadSerializer());
        gsonBuilder.registerTypeAdapterFactory(new ErrorHandlerTypeAdapterFactory());

        Gson gsonInstance = gsonBuilder.create();

        // Try serializing class

        Class<?> clazz = JsonSerializerTest.class;

        gsonInstance.toJson(clazz);

        gsonInstance.toJson(Thread.currentThread());
    }

}
