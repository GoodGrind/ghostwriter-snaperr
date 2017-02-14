package io.ghostwriter.rt.snaperr.json.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.ghostwriter.rt.snaperr.ErrorTriggerImpl;
import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.ReferenceTracker;
import io.ghostwriter.rt.snaperr.json.serializer.ClassSerializer;
import io.ghostwriter.rt.snaperr.json.serializer.ErrorHandlerTypeAdapterFactory;
import io.ghostwriter.rt.snaperr.json.serializer.JsonSerializer;
import io.ghostwriter.rt.snaperr.json.serializer.ThreadSerializer;
// FIXME (pal): StackbasedReferenceTracker should not be tested here
import io.ghostwriter.rt.snaperr.tracker.StackBasedReferenceTracker;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class JsonSerializerTest {

    /**
     * We don not include line number, timestamp and exception serialization because
     * they differ at each run. (Line number can be broken easily too by chaning the test)
     */
    @Test
    public void testJsonSerialization() {

        final boolean doPrettyPrint = false;
        final int stackTraceLimit = 1;
        final String appName = "appName";
        final JsonSerializer jsonSerializer = new JsonSerializer(appName, doPrettyPrint, stackTraceLimit);

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
        
        /*

        {
        	"data" : {
        		"type" : "exception",
        		"attributes" : {
        			"application_uuid" : "abcd1234",
        			"file" : "ErrenousSource.class",
        			"original_timestamp" : "1474577179",
        			"exception" : "NullPointerException",
        			"stacktrace" : "javax.servlet.ServletException: Something bad happened\n at com.example.myproject.OpenSessionInViewFilter.doFilter(OpenSessionInViewFilter.java:60)\n at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n ... 27 more\n Caused by: org.hibernate.exception.ConstraintViolationException: could not insert: [com.example.myproject.MyEntity]\n at org.hibernate.exception.SQLStateConverter.convert(SQLStateConverter.java:96)\n at org.hibernate.exception.JDBCExceptionHelper.convert(JDBCExceptionHelper.java:66)\n ... 32 more\n Caused by: java.sql.SQLException: Violation of unique constraint MY_ENTITY_UK_1: duplicate value(s) for column(s) MY_COLUMN in statement [...]\n at org.hsqldb.jdbc.Util.throwError(Unknown Source)\n at org.hsqldb.jdbc.jdbcPreparedStatement.executeUpdate(Unknown Source)\n ... 54 more",
        			"context" : {
        				"data" : [{
        						"class" : "OpenSessionInViewFilter",
        						"method" : "doFilter",
        						"variables" : [{
        								"value" : "Easy as 123?",
        								"type" : "Question",
        								"name" : "question"
        							}, {
        								"value" : 1,
        								"type" : "Integer",
        								"name" : "iterator"
        							}
        						]
        					}
        				]
        			},
        			"instance" : "instance@1234",
        			"line" : 1234
        		}
        	}
        }

         */
        
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
        
        JsonObject jData2 = jData.get("context").getAsJsonObject().get("data").getAsJsonObject();
        assertNotNull("class does not exist in the json", jData2.get("class"));
        assertNotNull("method does not exist in the json", jData2.get("method"));
        assertNotNull("variables does not exist in the json", jData2.get("variables"));
    }

    @Test
    public void testMultipleScopeInEntrySerialization() {

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

        ErrorTrigger errorTrigger = new ErrorTriggerImpl(referenceTracker, throwable);

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
