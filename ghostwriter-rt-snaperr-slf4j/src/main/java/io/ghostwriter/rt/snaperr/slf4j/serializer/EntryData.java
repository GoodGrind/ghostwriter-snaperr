package io.ghostwriter.rt.snaperr.slf4j.serializer;

import io.ghostwriter.rt.snaperr.serializer.Attributes;

public class EntryData {

    private String type;
    private Attributes attributes = new Attributes();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

}
