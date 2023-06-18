package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Frame {
    String code;
    String message;
    String userId;
    String cbcKey=null;
    String initVector=null;
    List<Message> messages;
    List<Chat> chats;
    PublicPrivateKeyImp pk;

    public Frame(){

    }
    public Frame(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String toJsonString(String userPublicKey, Encryptor encryptor){
        PublicKey publicKey = PublicPrivateKeyImp.getPublicKeyFromString(userPublicKey);


        JSONObject json = new JSONObject();
        HashMap<String,String> params = encryptor.getParams();
        for(String paramName : params.keySet()){
            try {
                String paramValue = params.get(paramName);
                String encryptedValue = PublicPrivateKeyImp.encrypt(paramValue,publicKey);
                json.put(paramName, encryptedValue);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            json.put("code", encryptor.encrypt(code));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if(userId!=null){
            try {
                json.put("user_id", encryptor.encrypt(userId));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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
