package io.ghostwriter.rt.snaperr.slf4j.serializer;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * This type adapter factory will delegate to the original
 * {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)} and invokes
 * it's write method. If that write method fails, then simply toString is
 * invoked on the object.
 *
 * @author pal
 */
public class ErrorHandlerTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            public void write(JsonWriter out, T value) throws IOException {
                try {
                    delegate.write(out, value);
                } catch (Exception e) {
                    out.value(String.valueOf(value));
                }
            }

            public T read(JsonReader in) throws IOException {
                throw new UnsupportedOperationException("Parsing JSON data is not supported");
            }
        };
    }

}
