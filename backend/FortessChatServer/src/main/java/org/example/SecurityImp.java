package org.example;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class SecurityImp implements SecurityInt {
    private KeyPair pair;

    private String privateKeyFileName = "private_key";
    private String publicKeyFileName = "public_key";

    private static volatile SecurityImp instance;

    public static SecurityImp getInstance() {
        SecurityImp result = instance;
        if(result != null){
            return result;
        }
        synchronized (SecurityImp.class){
            if(instance == null){
                instance = new SecurityImp();
            }
            return instance;
        }
    }
    private SecurityImp(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //generatePair();
        //savePair();
        loadPair();
    }

    @Override
    public PublicKey getPublicKey() {
        return pair.getPublic();
    }
    @Override
    public String encrypt(String message){
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE,getPublicKey());

            byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

            String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
            return encodedMessage;

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String message) {
        Cipher decryptCipher = null;
        try {
            decryptCipher = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            decryptCipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] decryptedMessageBytes;
        try {
            System.out.println("String length" + message.length());
            byte messageBytes [] = Base64.getDecoder().decode(message);
            System.out.println("Bytes length: " + messageBytes.length);
            decryptedMessageBytes = decryptCipher.doFinal(messageBytes);

        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return decryptedMessage;

    }
    private void generatePair()  {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        generator.initialize(4096);

        pair = generator.generateKeyPair();


    }
    private PrivateKey getPrivateKey(){
        return pair.getPrivate();
    }
    private void saveKey(Key key, String keyName)  {
        try (FileOutputStream fos = new FileOutputStream(keyName+ ".key")) {
            fos.write(key.getEncoded());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void savePrivateKey(PrivateKey privateKey){
        saveKey(privateKey, privateKeyFileName);
    }
    private void savePublicKey(PublicKey publicKey){
        saveKey(publicKey, publicKeyFileName);


        PemFile pemFile = new PemFile(publicKey, "RSA PUBLIC KEY");
        try {
            pemFile.write("server_public_key.pem");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void savePair(){
        savePrivateKey(pair.getPrivate());
        savePublicKey(pair.getPublic());

    }
    private void loadPair(){
        pair = new KeyPair(loadPublicKey(), loadPrivateKey());
    }
    private EncodedKeySpec loadKeySpec(String keyName, boolean isPrivate) {
        File keyFile = new File(keyName+ ".key");
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = Files.readAllBytes(keyFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        EncodedKeySpec keySpec;
        if(isPrivate){
            keySpec = new PKCS8EncodedKeySpec(keyBytes);
        }
        else{
            keySpec = new X509EncodedKeySpec(keyBytes);
        }

        return keySpec;

    }
    private PrivateKey loadPrivateKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec loadKeySpec = loadKeySpec(privateKeyFileName, true);
            return keyFactory.generatePrivate(loadKeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
    private PublicKey loadPublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec loadKeySpec = loadKeySpec(publicKeyFileName, false);
            return keyFactory.generatePublic(loadKeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}
