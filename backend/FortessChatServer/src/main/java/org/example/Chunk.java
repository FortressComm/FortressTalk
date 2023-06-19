package org.example;

public class Chunk {

    String chatId;
    int number;
    String fileName;
    byte [] bytes;

    public Chunk(String chatId, int number, String fileName, byte [] bytes) {

        this.chatId = chatId;
        this.number = number;
        this.fileName = fileName;
        this.bytes = bytes;
    }
}
