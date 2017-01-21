package io.ghostwriter.rt.snaperr.moroi;

public class MoroiEntryData {

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
