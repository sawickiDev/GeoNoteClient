package com.steveq.geonoteclient.login;

public class RegisterData {
    private String username;
    private String password;

    public RegisterData(){}

    public RegisterData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RegisterData{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
