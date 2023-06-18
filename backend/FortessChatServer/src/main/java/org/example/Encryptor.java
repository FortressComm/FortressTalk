package org.example;

import java.util.HashMap;

public interface Encryptor {
    String encrypt(String message);
    String decrypt(String message);

    byte[] encrypt(byte[] message);
    byte[] decrypt(byte[] message);

    //Todo byte encryption/decryption

    HashMap<String, String> getParams();
}
