package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CbcEncryptor implements Encryptor{
    private String cbcKey;
    private String initVector;
    private static String transformation = "AES/CBC/PKCS7PADDING";

    public CbcEncryptor(String cbcKey,String initVector ){
        this.cbcKey = cbcKey;
        this.initVector = initVector;
    }
    public String encrypt( String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector.getBytes("UTF-8")));
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(cbcKey.getBytes("UTF-8")), "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public  String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector.getBytes("UTF-8")));
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(cbcKey.getBytes("UTF-8")), "AES");

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode((encrypted)));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
