package com.steveq.geonoteclient.map;

import java.util.List;

public class GeoNoteBatch {
    private List<GeoNote> notes;

    public GeoNoteBatch() {}

    public GeoNoteBatch(List<GeoNote> notes) {
        this.notes = notes;
    }

    public List<GeoNote> getNotes() {
        return notes;
    }

    public void setNotes(List<GeoNote> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "GeoNoteBatch{" +
                "notes=" + notes +
                '}';
    }
}
