package io.ghostwriter.rt.snaperr.json.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Serializaton support for Thread object
 *
 * @author pal
 */
public class ThreadSerializer implements JsonSerializer<Runnable> {

    @Override
    public JsonElement serialize(Runnable src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

}
