package com.pruebatecnica.ventas.dto.auth;

public class LoginResponseDTO {

    private String token;
    private String type;
    private long expiresInMs;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String type, long expiresInMs) {
        this.token = token;
        this.type = type;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
