package io.ghostwriter.rt.snaperr.slf4j.serializer;

import io.ghostwriter.rt.snaperr.serializer.EntryData;

public class Entry {

    private EntryData data = new EntryData();

    public EntryData getData() {
        return data;
    }

    public void setData(EntryData data) {
        this.data = data;
    }

}
