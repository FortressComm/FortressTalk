package org.example;


import java.security.PublicKey;

public interface SecurityInt {
    PublicKey getPublicKey();
    String encrypt(String message);
    String decrypt(String message);


}
