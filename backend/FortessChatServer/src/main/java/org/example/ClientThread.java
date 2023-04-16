package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ClientThread implements Runnable {
    ClientThread(Socket clientSocket, MessageServer messageServer) {
        this.clientSocket = clientSocket;
        this.messageServer = messageServer;

    }
    private boolean isAuthorized = false;
    private User user = null;
    static final String CREATE_CHAT = "CREATE_CHAT";
    static final String JOIN_CHAT = "JOIN_CHAT:";
    static final String WRITE_TO_CHAT = "WRITE_TO_CHAT:";
    static final String REGISTER = "REGISTER:";
    static final String LOGIN = "LOGIN:";
    MessageServer messageServer;
   
    
    private Socket clientSocket;
    public OutputStream out;
    public InputStream in;

    public void run() {

        try {
            out = clientSocket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Port:" + clientSocket.getPort());
        System.out.println("out");

        try {
            in = clientSocket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("in");

        byte[] buffer = new byte[10000];
        int bytesRead = 0;

        while (true) {
            try {
                if (!((bytesRead = in.read(buffer)) >= 0)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bytesRead > 0) {
                var messageBytes = Arrays.copyOfRange(buffer, 0, bytesRead);
                String message = new String(messageBytes, StandardCharsets.UTF_8);

                if (message.startsWith(CREATE_CHAT)) {
                    if(isAuthorized){
                        createChat(message);
                        saveChats();
                    }
                    else{
                        sendNoAuthMessage();
                    }

                }
                else if(message.startsWith(REGISTER)){
                    if(!isAuthorized) {
                        register(message);
                        saveUsers();
                    }
                    else{
                        sendMessage(out, "You are already authorized");
                    }

                }
                else if(message.startsWith(LOGIN)){
                    if(!isAuthorized){
                        login(message);
                    }
                    else{
                        sendMessage(out, "You are already authorized");
                    }

                }
                else if (message.startsWith(JOIN_CHAT)) {
                    if(isAuthorized) {
                        joinChat(message);
                    }
                    else{
                        sendNoAuthMessage();
                    }

                }
                else if(message.startsWith(WRITE_TO_CHAT)){
                    if(isAuthorized){
                        writeToChat(message);
                    }
                    else{
                        sendNoAuthMessage();
                    }
                }
                else {
                    unknownMessage(message);
                }
            }
        }

    }
    private void sendNoAuthMessage(){
        sendMessage(out, "You are not authorized");
    }
    private void sendMessage(OutputStream out, String message){
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String getMessageWithoutPrefix(String message, String prefix){
        String text = message.substring(message.indexOf(prefix) + prefix.length());
        return text;
    }
    private String[] getMessageParams(String message){
        return message.split("&");
    }
    private void register(String message){
        String login;
        String password;
        try{
        String registerInfo = getMessageWithoutPrefix(message,REGISTER);
        String params[] = getMessageParams(registerInfo);
        login = params[0];
        password = params[1];
        }
        catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException exception){
            sendMessage(out, "Wrong number of parameters");
            return;
        }

        User user = new User(login, password);
        this.user = user;
        this.isAuthorized = true;
        messageServer.users.add(user);
        try {
            out.write("thank you for registration".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void login(String message){
        String login;
        String password;
        try{
        String registerInfo = getMessageWithoutPrefix(message,LOGIN);
        String params[] = getMessageParams(registerInfo);
            login = params[0];
            password = params[1];
        }
        catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException exception){
            sendMessage(out, "Wrong number of parameters");
            return;
        }

        Optional<User> user = messageServer.users.stream().filter(u-> u.login.equals(login)
                && u.password.equals(password)).findFirst();
        if(user.isPresent()){
            this.isAuthorized = true;
            this.user = user.get();
            sendMessage(out, "You are logged in");
        }
        else{

            sendMessage(out, "NO SUCH USER");
        }
    }


    private void unknownMessage(String message) {
        System.out.println(message);
        sendMessage(out, "UNKNOWN MESSAGE");
    }

    private void writeToChat(String message) {
        String text;
        String chatId;
        String textOfMessage;
        try {
            text = getMessageWithoutPrefix(message, WRITE_TO_CHAT);
            String params[] = getMessageParams(text);
            chatId = params[0];
            textOfMessage = params[1];
        }
            catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException exception){
            sendMessage(out, "Wrong number of parameters");
            return;
        }

        List<ClientThread> clientSockets = messageServer.clients;
        Optional<Chat> chat = getChatById(chatId);
        if(chat.isEmpty()){
           sendMessage(out, "NO SUCH CHAT");
           return;
        }

        if(!chat.get().userIds.contains(user.getId())){
            sendMessage(out, "YOU ARE NOT ALLOWED TO WRITE IN THIS CHAT");
            return;
        }

        for(ClientThread socket : clientSockets){
            if(clientSocket != socket.clientSocket && chat.get().userIds.contains(socket.user.getId())){

                sendMessage(socket.out, textOfMessage);
            }
            else{
                sendMessage(socket.out, "Message Was sent");
            }
        }
    }
    public Optional<Chat> getChatById(String uuid){
        Optional<Chat> c = messageServer.chats.stream().filter(chat -> chat.getId().equals(uuid)).findFirst();
        return c;
    }
    private void joinChat(String message) {
        String uuid = getMessageWithoutPrefix(message, JOIN_CHAT);
        Optional<Chat> c = getChatById(uuid);
        System.out.print(uuid);
        for(var chat : messageServer.chats){
            System.out.println("Other chats" + chat.getId());
        }
        if(c.isPresent()){
            c.get().userIds.add(this.user.getId());
            sendMessage(out, "You are added to a chat");

        }
        else{
            sendMessage(out, "NO SUCH CHAT");
        }
    }
    private void saveChats(){
        try {
            FileOutputStream fout = new FileOutputStream("chats");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(messageServer.chats);
            out.flush();

            out.close();
            fout.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void saveUsers(){
        try {
            FileOutputStream fout = new FileOutputStream("users");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(messageServer.users);
            out.flush();

            out.close();
            fout.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createChat(String message) {
        System.out.println(message);

        Chat chat = new Chat();
        chat.userIds.add(this.user.getId());
        messageServer.chats.add(chat);
        sendMessage(out, String.format("chat created %s", chat.getId()));
    }
}
