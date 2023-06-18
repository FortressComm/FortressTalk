package org.example;

import java.io.*;

import static org.example.ClientThread.*;

public class FileManager{
    String filename;
    long expectedSizeInBytes;
    ClientThread cT;
    FileManager(String filename, ClientThread clientThread, long expectedSizeInBytes){
        this.filename = filename;
        this.expectedSizeInBytes = expectedSizeInBytes;
        this.cT = clientThread;
    }
    public void appendChunk(byte[] data) {
        FileOutputStream output = null;
        try {

            output = new FileOutputStream(filename, true);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            output.write(data);


            File file = new File(filename);
            long actualSize = file.length();
            cT.sendFileProgress((actualSize*100)/expectedSizeInBytes);


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


