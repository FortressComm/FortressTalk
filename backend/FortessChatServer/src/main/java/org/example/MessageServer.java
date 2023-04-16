package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class MessageServer {
    public List<ClientThread> clients;
    public List<Chat> chats;
    public List<User> users;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public void start(int port) throws IOException {
        chats = new ArrayList<>();
        users = new ArrayList<>();
        clients = new ArrayList<ClientThread>();




        System.out.println("new server socket");
        serverSocket = new ServerSocket(port);
        // connecting clients:
        while(true){
            System.out.println("Trying to accept");
            clientSocket = serverSocket.accept();

            System.out.println("Accepted");
            ClientThread clientThread = new ClientThread(clientSocket, this);
            Thread thread = new Thread(clientThread);
            clients.add(clientThread);
            thread.start();
        }
    }
    private void loadChats(){
        try {
            ObjectInputStream in=new ObjectInputStream(new FileInputStream("chats"));
            this.chats = (List<Chat>) in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadUsers(){
        try {
            ObjectInputStream in=new ObjectInputStream(new FileInputStream("users"));
            this.users = (List<User>) in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadMessages(){

    }
    public static void main(String[] args) {
        MessageServer server=new MessageServer();
        try {
            server.start(65432);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
