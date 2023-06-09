package org.example;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    private String id;
    public String login;
    public String password;
    public String publicKey;

    public String getId() {
        return id;
    }

    public User(String login, String password, String publicKey) {
        this.login = login;
        this.password = password;
        this.publicKey = publicKey;
        this.id =  UUID.randomUUID().toString();
    }


}
