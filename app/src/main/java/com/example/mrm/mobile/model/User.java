package com.example.mrm.mobile.model;

public class User {
    private boolean auth;
    private String token;
    private int expiresIn;

    public boolean getAuth() {
        return this.auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
