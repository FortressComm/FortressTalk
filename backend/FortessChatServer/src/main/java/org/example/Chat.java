package org.example;

import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject toJsonString() {
        JSONObject json = new JSONObject();

        try {
            json.put("id", id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }
}
