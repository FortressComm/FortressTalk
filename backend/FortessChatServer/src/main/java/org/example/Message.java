package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Message implements Serializable {
    private String id;

    public String text;

    public Message(String text) {
        this.id =  UUID.randomUUID().toString();
        this.text = text;
    }
}
