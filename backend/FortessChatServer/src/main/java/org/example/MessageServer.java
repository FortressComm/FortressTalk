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
    public List<Message> messages;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public void start(int port) throws IOException {
        chats = new ArrayList<>();
        users = new ArrayList<>();
        messages = new ArrayList<>();
        clients = new ArrayList<ClientThread>();

        loadChats();
        loadUsers();
        loadMessages();


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
        } catch (IOException |ClassNotFoundException e) {
            System.out.println("No chats to be load");
        }
    }
    private void loadUsers(){
        try {
            ObjectInputStream in=new ObjectInputStream(new FileInputStream("users"));
            this.users = (List<User>) in.readObject();
        }catch (IOException |ClassNotFoundException e) {
            System.out.println("No users to be load");
        }
    }
    private void loadMessages(){
        try {
            ObjectInputStream in=new ObjectInputStream(new FileInputStream("messages"));
            this.messages = (List<Message>) in.readObject();

        }
        catch (IOException |ClassNotFoundException e) {
            System.out.println("No messages to be load");
        }
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
