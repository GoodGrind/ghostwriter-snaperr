package io.ghostwriter.rt.snaperr.json.serializer;


public class Entry {

    private EntryData data = new EntryData();

    public EntryData getData() {
        return data;
    }

    public void setData(EntryData data) {
        this.data = data;
    }

}
