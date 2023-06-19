package org.example;

import java.io.*;
import java.util.*;

import static org.example.ClientThread.*;

public class FileManager{
    List<Chunk> chunkList;
    String filename;
    long expectedSizeInBytes;
    ClientThread cT;
    FileManager(String filename, ClientThread clientThread, long expectedSizeInBytes){
        this.filename = filename;
        this.expectedSizeInBytes = expectedSizeInBytes;
        this.cT = clientThread;
        chunkList = new ArrayList<>();
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

            output = new FileOutputStream(filename, true);

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
    public void sendChunks(){
        FileInputStream is = null;
        try {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int chunkLen = 0;
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] chunk = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(chunk)) != -1) {
                Frame frame = new Frame(SERVER_CHUNK_SEND, "chunk sent");
                frame.chunk = chunk;
                cT.sendFrame( cT.user, cT.out, frame, cT.encryptor);
            }
            Frame frameEnd = new Frame(SERVER_CHUNK_END, "final chunk was sent enjoy");
            cT.sendFrame( cT.user, cT.out, frameEnd, cT.encryptor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        }

}


