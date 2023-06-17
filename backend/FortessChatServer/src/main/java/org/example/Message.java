package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Message implements Serializable {
    private String userFromId;
    private String chatId;
    private String id;
    private byte [] bytesMessage;



    public String getUserFromId() {
        return userFromId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getId() {
        return id;
    }

    public String text;

    public Message(String userFromId,String chatId, String text, byte[] bytesMessage) {
        this.userFromId = userFromId;
        this.chatId = chatId;
        this.id =  UUID.randomUUID().toString();
        this.text = text;
    }

    public JSONObject toJson(Encryptor encryptor){
        JSONObject json = new JSONObject();

        try {
            json.put("user_id_from", encryptor.encrypt(userFromId));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            json.put("id", encryptor.encrypt(id));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            json.put("text", encryptor.encrypt(text));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
