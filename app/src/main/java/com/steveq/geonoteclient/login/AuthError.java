package com.steveq.geonoteclient.login;

import com.google.gson.annotations.SerializedName;

public class AuthError {
    @SerializedName("error")
    private String error;
    @SerializedName("error_description")
    private String errorDescription;

    public AuthError(){}

    public AuthError(String error, String error_description) {
        this.error = error;
        this.errorDescription = error_description;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String error_description) {
        this.errorDescription = error_description;
    }

    @Override
    public String toString() {
        return "AuthError{" +
                "error='" + error + '\'' +
                ", error_description='" + errorDescription + '\'' +
                '}';
    }
}
