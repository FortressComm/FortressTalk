package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CbcEncryptor implements Encryptor{
    private byte[] cbcKey;
    private byte[] initVector;
    private static String transformation = "AES/CBC/PKCS7PADDING";

    public CbcEncryptor(byte[] cbcKey,byte[] initVector ){
        this.cbcKey = cbcKey;
        this.initVector = initVector;
    }

    @Override
    public HashMap<String, String> getParams() {
        var paramMap = new HashMap<String, String>();
        paramMap.put("initVector",initVector.toString() );
        paramMap.put("cbcKey",cbcKey.toString() );
        return paramMap;
    }
    @Override
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(cbcKey, "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    @Override
    public byte[] encrypt(byte[] message){
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(cbcKey, "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(message);
            return Base64.getEncoder().encode(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    @Override
    public  String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(cbcKey, "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode((encrypted)));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    @Override
    public  byte[] decrypt( byte[] encryptedMessage) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(cbcKey, "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode((encryptedMessage)));

            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
