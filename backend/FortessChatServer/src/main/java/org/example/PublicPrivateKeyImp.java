package org.example;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

public final class PublicPrivateKeyImp implements Encryptor {
    private KeyPair pair;

    @Override
    public HashMap<String, String> getParams() {
        return new HashMap<String, String>();
    }

    private String privateKeyFileName = "private_key";
    private String publicKeyFileName = "public_key";

    private static volatile PublicPrivateKeyImp instance;

    private static final String conf = "RSA/None/OAEPWithSHA256AndMGF1Padding";


    public static PublicPrivateKeyImp getInstance() {
        PublicPrivateKeyImp result = instance;
        if(result != null){
            return result;
        }
        synchronized (PublicPrivateKeyImp.class){
            if(instance == null){
                instance = new PublicPrivateKeyImp();
            }
            return instance;
        }
    }

    public static PublicKey getPublicKeyFromString(String key){
        Security.addProvider(new BouncyCastleProvider());
        try{
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\n","");
            byte[] byteKey =  Base64.getDecoder().decode(publicKeyPEM.getBytes(StandardCharsets.UTF_8));

            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Invalid key: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("RSA algorithm not supported: " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            System.err.println("Invalid key specification: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }

        return null;
    }
    private PublicPrivateKeyImp(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //generatePair();
        //savePair();
        loadPair();
    }

    public PublicKey getPublicKey() {
        return pair.getPublic();
    }


    public static String encrypt(String message,PublicKey publicKey){
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance(conf);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

            String encodedMessage = new String(Base64.getEncoder().encode(encryptedMessageBytes), StandardCharsets.UTF_8);
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
    public byte[] decrypt(byte[] message) {
        Cipher decryptCipher = null;
        try {
            decryptCipher = Cipher.getInstance(conf);
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
            //System.out.println("String length" + message.length());
            byte messageBytes [] = Base64.getDecoder().decode(message);
            System.out.println("Bytes length: " + messageBytes.length);
            decryptedMessageBytes = decryptCipher.doFinal(messageBytes);

        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedMessageBytes;

    }
    @Override
    public String encrypt(String message){
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance(conf);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

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
            decryptCipher = Cipher.getInstance(conf);
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

    @Override
    public byte[] encrypt(byte[] message) {
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance(conf);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

            byte[] secretMessageBytes = message;
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);


            return Base64.getEncoder().encode(encryptedMessageBytes);

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
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
