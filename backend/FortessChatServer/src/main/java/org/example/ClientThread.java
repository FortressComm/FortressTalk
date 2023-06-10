package org.example;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ClientThread implements Runnable {
    ClientThread(Socket clientSocket, MessageServer messageServer) {
        this.clientSocket = clientSocket;
        this.messageServer = messageServer;


        securityImp = SecurityImp.getInstance();


    }
    private SecurityImp securityImp;
    private boolean isAuthorized = false;
    private User user = null;
    static final String CREATE_CHAT = "CREATE_CHAT";
    static final String JOIN_CHAT = "JOIN_CHAT";
    static final String WRITE_TO_CHAT = "WRITE_TO_CHAT";
    static final String REGISTER = "REGISTER";
    static final String LOGIN = "LOGIN";
    static final String GET_CHAT_MESSAGES = "GET_CHAT_MESSAGES";



    ////////////////////////////////////////////////////////////
    static final String SERVER_AUTH_CODE = "SERVER_AUTH_CODE";
    static final String SERVER_MSG_ALL = "SERVER_MSG_ALL";
    static final String SERVER_LOGIN = "SERVER_LOGIN";
    static final String SERVER_REGISTRATION = "SERVER_REGISTRATION";
    static final String SERVER_UNKNOWN = "SERVER_UNKNOWN";
    static final String SERVER_SECURITY = "SERVER_SECURITY";
    static final String SERVER_NEW_MESSAGE = "SERVER_NEW_MESSAGE";
    static final String SERVER_CHAT_ID = "SERVER_CHAT_ID";
    static final String SERVER_MSG_CHAT = "SERVER_MSG_CHAT";



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

        byte[] buffer = new byte[100000];
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

                String code;
                String text;
                String login;
                String password;
                String chatId;

                // JSON
                try {
                    JSONObject json = new JSONObject(message);
                    code = securityImp.decrypt(String.valueOf(json.getString("code")));


                    text = String.valueOf(json.getString("text"));
                    login = securityImp.decrypt(String.valueOf(json.getString("login")));





                    password = securityImp.decrypt(String.valueOf(json.getString("password")));
                    chatId = securityImp.decrypt(String.valueOf(json.getString("chat_id")));



                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }



                if (code.equals(CREATE_CHAT)) {
                    if(isAuthorized){
                        createChat();
                        saveChats();
                    }
                    else{
                        sendNoAuthMessage();
                    }

                }
                else if(code.equals(REGISTER)){
                    if(!isAuthorized) {
                        register(login, password);
                        saveUsers();
                    }
                    else{
                        sendFrame(out, new Frame(SERVER_AUTH_CODE, "You are already authorized"));
                    }

                }
                else if(code.equals(LOGIN)){
                    if(!isAuthorized){
                        login(login, password);
                    }
                    else{
                        sendFrame(out, new Frame(SERVER_AUTH_CODE, "You are already authorized"));
                    }

                }
                else if (code.equals(JOIN_CHAT)) {
                    if(isAuthorized) {
                        joinChat(chatId);
                        saveChats();
                    }
                    else{
                        sendNoAuthMessage();
                    }

                }
                else if(code.equals(WRITE_TO_CHAT)){
                    if(isAuthorized){
                        writeToChat(chatId, text,messageBytes);

                    }
                    else{
                        sendNoAuthMessage();
                    }
                }
                else if(code.equals(GET_CHAT_MESSAGES)){
                    if(isAuthorized){
                        List<Message> messages = getAllMessagesFromChat(chatId);

                        Frame frame = new Frame(SERVER_MSG_ALL, "");
                        frame.messages = messages;
                        sendFrame(out, frame);
                    }
                    else{
                        sendNoAuthMessage();
                    }
                }
                else {

                    unknownMessage(messageBytes);
                }
            }
        }

    }
    private void sendNoAuthMessage(){
        Frame frame = new Frame(SERVER_AUTH_CODE, "You are not authorized");
        sendFrame(out, frame);
    }

    private void sendFrame(OutputStream out, Frame frame) {

        try {
            out.write(frame.toJsonString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    private String getMessageWithoutPrefix(String message, String prefix){
//        String text = message.substring(message.indexOf(prefix) + prefix.length());
//        return text;
//    }
    private String[] getMessageParams(String message){
        return message.split("&");
    }
    private void register(String login, String password){

        User user = new User(login, password);
        this.user = user;
        this.isAuthorized = true;
        messageServer.users.add(user);
        Frame frame = new Frame(SERVER_REGISTRATION, "thank you for registration");
        sendFrame(out, frame);

    }
    private void login(String login, String password){
        Optional<User> user = messageServer.users.stream().filter(u-> u.login.equals(login)
                && u.password.equals(password)).findFirst();
        if(user.isPresent()){
            this.isAuthorized = true;
            this.user = user.get();
            Frame frame = new Frame(SERVER_LOGIN, "You are logged in");
            sendFrame(out, frame);
        }
        else{
            Frame frame = new Frame(SERVER_LOGIN, "NO SUCH USER");
            sendFrame(out, frame);
        }
    }


    private void unknownMessage(byte[] bt) {

        try {
            out.write(bt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToChat(String chatId, String text, byte [] textBytes) {



        Optional<Chat> chat = getChatById(chatId);
        if(chat.isEmpty()){
            Frame frame = new Frame(SERVER_UNKNOWN, "NO SUCH CHAT");
           return;
        }
        List<ClientThread> clientSockets = messageServer.clients;
        if(!chat.get().userIds.contains(user.getId())){
            Frame frame = new Frame(SERVER_SECURITY, "YOU ARE NOT ALLOWED TO WRITE IN THIS CHAT");
            sendFrame(out, frame);
            return;
        }
        Message m = new Message(user.getId(), chatId, text, textBytes);
        messageServer.messages.add(m);
        saveMessages();

        for(ClientThread socket : clientSockets){
            if(clientSocket != socket.clientSocket && chat.get().userIds.contains(socket.user.getId())){
                Frame frame = new Frame(SERVER_NEW_MESSAGE, text);
                sendFrame(socket.out, frame);
            }
            else{
                Frame frame = new Frame(SERVER_MSG_CHAT, "Message Was sent");
                sendFrame(socket.out, frame);
            }
        }
    }
    public List<Message> getAllMessagesFromChat(String chatId){

        Optional<Chat> chat = getChatById(chatId);
        if(chat.isEmpty()){
            Frame frame = new Frame(SERVER_UNKNOWN,"NO SUCH CHAT");
            sendFrame(out, frame);
            return new ArrayList<>();
        }
        List<ClientThread> clientSockets = messageServer.clients;
        if(!chat.get().userIds.contains(user.getId())){
            Frame frame = new Frame(SERVER_SECURITY,"YOU ARE NOT ALLOWED TO WRITE IN THIS CHAT" );
            sendFrame(out,frame );
            return new ArrayList<>();
        }
        return  messageServer.messages.stream().filter(message -> message.getChatId().equals(chatId)).toList();


    }
    public Optional<Chat> getChatById(String uuid){
        Optional<Chat> c = messageServer.chats.stream().filter(chat -> chat.getId().equals(uuid)).findFirst();
        return c;
    }
    private void joinChat(String chatId) {

        Optional<Chat> c = getChatById(chatId);
        System.out.print(chatId);
        for(var chat : messageServer.chats){
            System.out.println("Other chats" + chat.getId());
        }

        if(c.isPresent()){
            c.get().userIds.add(this.user.getId());

            Frame frame = new Frame(SERVER_MSG_CHAT, "You are added to a chat");
            sendFrame(out, frame);
        }
        else{
            Frame frame = new Frame(SERVER_UNKNOWN, "NO SUCH CHAT");
            sendFrame(out, frame);
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
    private void saveMessages(){
        try {
            FileOutputStream fout = new FileOutputStream("messages");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(messageServer.messages);
            out.flush();

            out.close();
            fout.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createChat() {


        Chat chat = new Chat();
        chat.userIds.add(this.user.getId());
        messageServer.chats.add(chat);
        Frame frame = new Frame(SERVER_CHAT_ID,chat.getId() );
        sendFrame(out, frame);

        joinChat(chat.getId());
    }
}
