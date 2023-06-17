package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Frame {
    String code;
    String message;
    String cbcKey=null;
    String initVector=null;
    List<Message> messages;
    List<Chat> chats;


    public Frame(){

    }
    public Frame(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String toJsonString(Encryptor encryptor){
        JSONObject json = new JSONObject();

        try {
            json.put("code", encryptor.encrypt(code));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            json.put("cbcKey", encryptor.encrypt(cbcKey));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            json.put("initVector", encryptor.encrypt(initVector));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        try {
            json.put("text", encryptor.encrypt(message));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            if(messages != null){

                json.put("messages", new JSONArray(messages.stream().map(message ->message.toJson(encryptor)).toList()) );
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try{
            if(chats!=null){
                json.put("chats", new JSONArray(chats.stream().map(chat -> chat.toJson(encryptor)).toList()));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return json.toString();
    }
}
