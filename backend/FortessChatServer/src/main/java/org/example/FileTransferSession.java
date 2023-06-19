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
    String directory ="files";
    String filePath ;
    ClientThread cT;
    int actualSize=0;
    FileTransferSession(String filename,ClientThread clientThread,String chatId){
        this.fileName = filename;
        this.cT = clientThread;
        chunkList = new ArrayList<>();
        this.chatId = chatId;
        actualSize =0;
        this.filePath = directory+'/'+fileName;
    }
    FileTransferSession(String filename,ClientThread clientThread, long expectedSizeInBytes,String chatId){
        this.fileName = filename;
        this.expectedSizeInBytes = expectedSizeInBytes;
        this.cT = clientThread;
        chunkList = new ArrayList<>();
        this.chatId = chatId;
        this.filePath = directory+'/'+fileName;
    }
    public void appendChunk(Chunk chunk) {
        chunkList.add(chunk);

        actualSize+=chunk.bytes.length;
        cT.sendFileProgress((actualSize*100)/expectedSizeInBytes);
    }
    public void appendChunkToFile(byte[] data){
        FileOutputStream output = null;
        try {

            output = new FileOutputStream(filePath, true);

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

        if(fileName.contains("/")){
            System.out.println("file contains /");
            return;
        }
        Path path;
        try {
            path = Paths.get(filePath);
            long fileSize = Files.size(path);
            this.expectedSizeInBytes = fileSize;
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        Frame frameStart = new Frame(SERVER_START_SEND, "I will send chunk to you");

        frameStart.fileName = fileName;
        cT.sendFrame( cT.user, cT.out, frameStart, cT.encryptor);




        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] chunk = new byte[10240];
            int allBytesRead = 0;
            int bytesRead;
            int chunkNumber =0;
            while ((bytesRead = fis.read(chunk)) != -1) {
                Frame frame = new Frame(SERVER_CHUNK_SEND, "chunk sent");
                frame.chunk = chunk;
                frame.chatId = chatId;
                frame.fileName = fileName;
                frame.chunkNumber = String.valueOf(chunkNumber);
                chunkNumber+=1;
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


