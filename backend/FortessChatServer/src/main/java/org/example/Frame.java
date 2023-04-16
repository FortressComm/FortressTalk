package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame {
    String code;
    String message;
    List<Message> messages;
    public Frame(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String toJsonString(){
        JSONObject json = new JSONObject();

        try {
            json.put("code", code);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            json.put("text", message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            if(messages != null){
                json.put("messages", messages.stream().map(message ->message.toJsonString()).toList() );
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return json.toString();
    }
}