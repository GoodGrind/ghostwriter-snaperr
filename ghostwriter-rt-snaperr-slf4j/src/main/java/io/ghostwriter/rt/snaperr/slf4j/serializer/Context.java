package io.ghostwriter.rt.snaperr.slf4j.serializer;

import java.util.LinkedList;
import java.util.List;

import io.ghostwriter.rt.snaperr.serializer.ContextData;

public class Context {

    private List<ContextData> data = new LinkedList<>();

    public List<ContextData> getData() {
        return data;
    }

}
