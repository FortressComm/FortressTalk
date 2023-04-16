package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Chat implements Serializable {
    private String id;

    public String getId() {
        return id;
    }

    public List<String> userIds;
    Chat() {
        this.id =  UUID.randomUUID().toString();
        this.userIds = new ArrayList<>();
    }
}
