package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.example.ClientThread.*;

public class FileTransferSession {
    private String chatId;
    List<Chunk> chunkList;
    String fileName;
    long expectedSizeInBytes;
    ClientThread cT;
    FileTransferSession(String filename,ClientThread clientThread,String chatId){
        this.fileName = filename;
        this.cT = clientThread;
        chunkList = new ArrayList<>();
        this.chatId = chatId;
    }
    FileTransferSession(String filename,ClientThread clientThread, long expectedSizeInBytes,String chatId){
        this.fileName = filename;
        this.expectedSizeInBytes = expectedSizeInBytes;
        this.cT = clientThread;
        chunkList = new ArrayList<>();
        this.chatId = chatId;
    }
    public void appendChunk(Chunk chunk) {
        chunkList.add(chunk);
        var actualSize=0;
        for(var c : chunkList){
            actualSize+=c.bytes.length;
        }
        cT.sendFileProgress((actualSize*100)/expectedSizeInBytes);
    }
    public void appendChunkToFile(byte[] data){
        FileOutputStream output = null;
        try {

            output = new FileOutputStream(fileName, true);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            output.write(data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void close(){
        Collections.sort(chunkList, Comparator.comparingInt(c -> c.number));
        byte fileBytes[] = new byte[]{};
        for ( var c :chunkList){
            appendChunkToFile(c.bytes);
        }
    }
    public void sendChunksToMe(){
        Path path = Paths.get(fileName);
        try {
            long fileSize = Files.size(path);
            this.expectedSizeInBytes = fileSize;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Frame frameStart = new Frame(SERVER_START_SEND, "I will send chunk to you");

        frameStart.fileName = fileName;
        cT.sendFrame( cT.user, cT.out, frameStart, cT.encryptor);

        FileInputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        try (FileInputStream fis = new FileInputStream(fileName)) {
            byte[] chunk = new byte[1024];
            int allBytesRead = 0;
            int bytesRead;
            while ((bytesRead = fis.read(chunk)) != -1) {
                Frame frame = new Frame(SERVER_CHUNK_SEND, "chunk sent");
                frame.chunk = chunk;
                frame.chatId = chatId;
                frame.fileName = fileName;
                cT.sendFrame( cT.user, cT.out, frame, cT.encryptor);
                allBytesRead+=bytesRead;
                cT.sendFileProgress((allBytesRead*100)/expectedSizeInBytes);

            }
            Frame frameEnd = new Frame(SERVER_END_SEND, "final chunk was sent enjoy");
            frameEnd.fileName = fileName;
            cT.sendFrame( cT.user, cT.out, frameEnd, cT.encryptor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        }

}


