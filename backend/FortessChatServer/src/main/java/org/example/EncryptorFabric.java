package org.example;

public class EncryptorFabric {
    public static Encryptor createEncryptor(String type){
        if(type.equals("cbc")){
            //todo
            return new CbcEncryptor("","");
        }
        return null;
    }
}
