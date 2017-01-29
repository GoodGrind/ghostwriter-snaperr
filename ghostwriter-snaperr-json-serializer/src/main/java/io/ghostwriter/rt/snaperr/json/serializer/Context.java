package io.ghostwriter.rt.snaperr.json.serializer;

import java.util.LinkedList;
import java.util.List;


public class Context {

    private List<ContextData> data = new LinkedList<>();

    public List<ContextData> getData() {
        return data;
    }

}
