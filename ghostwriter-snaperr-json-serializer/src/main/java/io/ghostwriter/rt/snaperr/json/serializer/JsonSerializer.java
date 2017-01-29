package io.ghostwriter.rt.snaperr.json.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

import java.util.Objects;

// FIXME(snorbi07): needs to be aligned with Moroi format and the impl. refactored
public class JsonSerializer implements TriggerSerializer<String> {

    private final Gson gson;

    /*
     * We probably should reintroduce it...
     */
    private final Integer stackTraceLimit;

    private final String applicationUUID;

    public JsonSerializer(String applicationUUID) {
        this(applicationUUID, true, null);
    }

    public JsonSerializer(String applicationUUID, boolean doPrettyPrint, Integer stackTraceLimit) {
        this.applicationUUID = applicationUUID;
        gson = createGsonInstance(doPrettyPrint);
        if (stackTraceLimit != null && stackTraceLimit < 0) {
            throw new IllegalArgumentException("Stack trace limit must be a positive number! Got: "
                    + stackTraceLimit);
        }
        this.stackTraceLimit = stackTraceLimit;
    }

    Gson createGsonInstance(boolean doPrettyPrint) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.enableComplexMapKeySerialization();
        if (doPrettyPrint) {
            gsonBuilder.setPrettyPrinting();
        }

        gsonBuilder.registerTypeAdapter(Class.class, new ClassSerializer());
        gsonBuilder.registerTypeAdapter(Thread.class, new ThreadSerializer());
        gsonBuilder.registerTypeAdapterFactory(new ErrorHandlerTypeAdapterFactory());

        return gsonBuilder.create();
    }

    @Override
    public String serializeTrigger(ErrorTrigger errorTrigger) {
        Objects.requireNonNull(errorTrigger);

		/* FIXME (pal) : check performance, it will most certainly be faster if we hand-build the JSON
         * But perhaps does not even matter if we anyways json serialize the scope variables :'(
		 */

        EntryMapper moroiEntryMapper = new EntryMapper(applicationUUID);
        Entry moroiEntry = moroiEntryMapper.toMoroiEntry(errorTrigger);

        final JsonElement jsonElement = gson.toJsonTree(moroiEntry.getData());
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("data", jsonElement);
        final String json = jsonObject.toString();

        return json;
    }

    @Override
    public String serializeTrigger(TimeoutTrigger timeoutTrigger) {
        throw new UnsupportedOperationException("missing implementation");
    }

}
