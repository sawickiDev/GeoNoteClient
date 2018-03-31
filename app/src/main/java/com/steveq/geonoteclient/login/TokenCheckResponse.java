package com.steveq.geonoteclient.login;

public class TokenCheckResponse {
    private Boolean active;

    public TokenCheckResponse(){}

    public TokenCheckResponse(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "TokenCheckResponse{" +
                "active='" + active + '\'' +
                '}';
    }
}
