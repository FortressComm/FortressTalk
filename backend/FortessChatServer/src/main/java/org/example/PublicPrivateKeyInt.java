package org.example;


import java.security.PublicKey;

public interface PublicPrivateKeyInt {
    PublicKey getPublicKey();
    String encrypt(String message);
    String decrypt(String message);



}
