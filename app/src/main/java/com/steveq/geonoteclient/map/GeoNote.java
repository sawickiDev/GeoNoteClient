package com.steveq.geonoteclient.map;

public class GeoNote {
    private String note;
    private Double lat;
    private Double lng;
    private String owner;
    private String error;

    public GeoNote() {}

    public GeoNote(String note, Double lat, Double lng, String owner) {
        this.note = note;
        this.lat = lat;
        this.lng = lng;
        this.owner = owner;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getOwner() {
        if(owner == null)
            return "UNKNOWN";
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "GeoNote{" +
                "note='" + note + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", owner='" + owner + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
