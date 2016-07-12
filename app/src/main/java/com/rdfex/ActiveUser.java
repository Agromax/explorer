package com.rdfex;

/**
 * Created by Dell on 12-07-2016.
 */
public class ActiveUser {
    private final String name;
    private final String email;
    private final String userId;
    private final String sessionToken;

    public ActiveUser(String name, String email, String id, String token) {
        this.name = name;
        this.email = email;
        this.userId = id;
        this.sessionToken = token;

    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
