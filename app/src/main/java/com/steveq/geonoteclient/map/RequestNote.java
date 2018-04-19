package com.steveq.geonoteclient.map;

public class RequestNote {

    private String note;
    private Double lat;
    private Double lng;
    private Integer expirationMinutes;

    public RequestNote(){}

    public RequestNote(String note, Double lat, Double lng, Integer expirationMinutes) {
        this.note = note;
        this.lat = lat;
        this.lng = lng;
        this.expirationMinutes = expirationMinutes;
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

    public Integer getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(Integer expirationTime) {
        this.expirationMinutes = expirationTime;
    }

    @Override
    public String toString() {
        return "RequestNote{" +
                "note='" + note + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", expirationMinutes=" + expirationMinutes +
                '}';
    }
}
