package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ClientThread implements Runnable {
    ClientThread(Socket clientSocket, MessageServer messageServer) {
        this.clientSocket = clientSocket;
        this.messageServer = messageServer;


        pkpk = PublicPrivateKeyImp.getInstance();


    }
    private PublicPrivateKeyImp pkpk;
    private boolean isAuthorized = false;
    public User user = null;
    static final String CREATE_CHAT = "CREATE_CHAT";
    static final String JOIN_CHAT = "JOIN_CHAT";

    static final String GET_CHATS = "GET_CHATS";
    static final String WRITE_TO_CHAT = "WRITE_TO_CHAT";
    static final String REGISTER = "REGISTER";
    static final String LOGIN = "LOGIN";

    static final String START_SEND = "START_SEND";
    static final String SEND = "SEND";
    static final String END_SEND = "END_SEND";

    static final String GET_CHAT_MESSAGES = "GET_CHAT_MESSAGES";



    ////////////////////////////////////////////////////////////
    static final String SERVER_AUTH_CODE = "SERVER_AUTH_CODE";
    static final String SERVER_MSG_ALL = "SERVER_MSG_ALL";
    static final String SERVER_LOGIN = "SERVER_LOGIN";
    static final String SERVER_CHATS = "SERVER_CHATS";


    static final String SERVER_LOGIN_FAILED = "SERVER_LOGIN_FAILED";
    static final String SERVER_REGISTRATION = "SERVER_REGISTRATION";
    static final String SERVER_UNKNOWN = "SERVER_UNKNOWN";
    static final String SERVER_SECURITY = "SERVER_SECURITY";
    static final String SERVER_NEW_MESSAGE = "SERVER_NEW_MESSAGE";
    static final String SERVER_CHAT_ID = "SERVER_CHAT_ID";
    static final String SERVER_MSG_CHAT = "SERVER_MSG_CHAT";

    static final String SERVER_JOINED_CHAT = "SERVER_JOINED_CHAT";

    static final String SERVER_FILE_PROGRESS = "SERVER_FILE_PROGRESS";
    static final String SERVER_CHUNK_SEND = "SERVER_CHUNK_SEND";

    static final String SERVER_CHUNK_END = "SERVER_CHUNK_END";

    MessageServer messageServer;
   
    
    private Socket clientSocket;
    public Encryptor encryptor;

    public OutputStream out;
    public InputStream in;
    public FileManager fileManager;

    private static List<byte[]> splitBytes(byte[] bytes) {
        List<byte[]> jsonBytesList = new ArrayList<>();
        int start = 0;
        int nestingLevel = 0;

        for (int i = 0; i < bytes.length; i++) {
            if (nestingLevel < 0){
                throw new RuntimeException("minus");
            }
            if (bytes[i] == '{') {
                nestingLevel++;

            } else if (bytes[i] == '}') {
                nestingLevel--;
                if (nestingLevel == 0) {
                    int end = i + 1;
                    byte[] jsonBytes = new byte[end - start];
                    System.arraycopy(bytes, start, jsonBytes, 0, end - start);
                    jsonBytesList.add(jsonBytes);
                    start = end;
                }
            }
        }

        return jsonBytesList;
    }
    private static boolean isCompleteJSON(byte[] jsonData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readTree(jsonData);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private static int getTotalBytesLength(List<byte[]> byteList) {
        int totalBytes = 0;
        for (byte[] byteArray : byteList) {
            totalBytes += byteArray.length;
        }
        return totalBytes;
    }
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

        int offset = 0;
        while (true) {
            try {
                if ((bytesRead = in.read(buffer, offset, 100000 - offset)) < 0) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bytesRead > 0) {
                var bufferReadBytes = Arrays.copyOfRange(buffer, 0, bytesRead + offset);
                String bufferReadBytesStr = new String(bufferReadBytes, StandardCharsets.UTF_8);

                List<byte[]> splitedBytes =splitBytes(bufferReadBytes);
                int normalBytesLen = getTotalBytesLength(splitedBytes);

                if(normalBytesLen != bufferReadBytes.length){

                    byte brokenJson[] = Arrays.copyOfRange(bufferReadBytes, normalBytesLen, bufferReadBytes.length);
                    String broken = new String(brokenJson, StandardCharsets.UTF_8);
                    int brokenJsonLen = brokenJson.length;
                    offset = brokenJsonLen;

                    buffer = new byte[100000];
                    ByteBuffer sourceBuffer = ByteBuffer.wrap(brokenJson);
                    ByteBuffer destinationBuffer = ByteBuffer.wrap(buffer);
                    destinationBuffer.put(sourceBuffer);
                    String bufferStr = new String(buffer, StandardCharsets.UTF_8);
                    bufferStr = bufferStr;
                }
                else{
                    offset =0;
                }




                for(var messageBytes : splitedBytes){
                    //

                    String message = new String(messageBytes, StandardCharsets.UTF_8);

                    String code = "";
                    String text = "";
                    String login = "";
                    String password = "";
                    String chatId = "";
                    String clientPublicKey = "";
                    String encryptionType = "";
                    String fileName = "";
                    int chunkNumber = 0;
                    long expectedSizeInBytes = 0;
                    byte[] chunkBytes = new byte[]{};;

                    // JSON
                    try {
                        JSONObject json = new JSONObject(message);


                        if(json.has("encryption_mode")){
                            encryptionType = pkpk.decrypt(String.valueOf(json.getString("encryption_mode")));
                            if(encryptionType.equals("CBC")){
                                byte[] cbcKey = new byte[]{};
                                byte[] initVector =new byte[]{};
                                if(json.has("key")){
                                    cbcKey = pkpk.decrypt(json.getString("key").getBytes());
                                }
                                if(json.has("iv")){
                                    initVector = pkpk.decrypt(json.getString("iv").getBytes());
                                }
                                encryptor = new CbcEncryptor(cbcKey, initVector);
                            }
                            else{

                            }
                        }
                        if(json.has("chunk")){
                            chunkBytes = encryptor.decrypt(json.getString("chunk").getBytes());
                        }

                        if(json.has("expected_size")){
                            expectedSizeInBytes =  Long.valueOf(encryptor.decrypt(json.getString("expected_size")));
                        }
                        if(json.has("file_name")){
                            fileName = encryptor.decrypt(String.valueOf(json.getString("file_name")));
                        }
                        if(json.has("chunk_number")){
                            chunkNumber = Integer.valueOf(encryptor.decrypt(json.getString("chunk_number")));
                        }
                        if(json.has("text")){
                            text = encryptor.decrypt(String.valueOf(json.getString("text")));
                        }
                        if(json.has("login")) {
                            login = encryptor.decrypt(String.valueOf(json.getString("login")));

                        }
                        if(json.has("password")) {
                            password = encryptor.decrypt(String.valueOf(json.getString("password")));

                        }
                        if(json.has("chat_id")){
                            chatId = encryptor.decrypt(String.valueOf(json.getString("chat_id")));
                        }
                        if(json.has("client_public_key")){
                            clientPublicKey = encryptor.decrypt(String.valueOf(json.getString("client_public_key")));
                        }
                        if(json.has("code")) {
                            code = encryptor.decrypt(String.valueOf(json.getString("code")));
                        }


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
                    else if(code.equals(START_SEND)){
                        fileManager = new FileManager(fileName, this, expectedSizeInBytes);
                    }
                    else if(code.equals(SEND)){
                        Chunk chunk= new Chunk(chatId, chunkNumber,fileName,chunkBytes);
                        fileManager.appendChunk(chunk);

                    }
                    else if(code.equals(END_SEND)){
                        fileManager.close();
                    }
                    else if(code.equals(REGISTER)){
                        if(!isAuthorized) {
                            register(login, password, clientPublicKey);
                            saveUsers();
                        }
                        else{
                            sendFrame(this.user,out, new Frame(SERVER_AUTH_CODE, "You are already authorized"),encryptor);
                        }

                    }
                    else if(code.equals(LOGIN)){
                        if(!isAuthorized){
                            login(login, password, clientPublicKey);
                        }
                        else{
                            sendFrame(this.user,out, new Frame(SERVER_AUTH_CODE, "You are already authorized"),encryptor);
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
                            sendFrame(this.user,out, frame,encryptor);
                        }
                        else{
                            sendNoAuthMessage();
                        }
                    }
                    else if(code.equals(GET_CHATS)){
                        if(isAuthorized){
                            List<Chat> chats = getAllChatsByUser();
                            Frame frame = new Frame(SERVER_CHATS, "");
                            frame.chats = chats;
                            sendFrame(this.user,out, frame,encryptor);
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

    }

    public void sendFileProgress(long progress){
        Frame frame = new Frame(SERVER_FILE_PROGRESS, "Your file progress");
        frame.progress = Long.toString(progress);
    }

    private List<Chat> getAllChatsByUser() {
        return messageServer.chats.stream().filter(chat -> chat.userIds.contains(user.getId())).toList();
    }

    private void sendNoAuthMessage(){
        Frame frame = new Frame(SERVER_AUTH_CODE, "You are not authorized");
        sendFrame(this.user,out, frame,encryptor);
    }

    public void sendFrame(User user,OutputStream out, Frame frame, Encryptor encryptor) {

        try {
            out.write(frame.toJsonString(user.publicKey, encryptor).getBytes(StandardCharsets.UTF_8));
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

    private void register(String login, String password,String publicKey){

        User user = new User(login, password, publicKey);
        this.user = user;
        this.isAuthorized = true;
        messageServer.users.add(user);
        Frame frame = new Frame(SERVER_REGISTRATION, "thank you for registration");
        sendFrame(this.user, out, frame,encryptor);

    }
    private void login(String login, String password, String publicKey){
        Optional<User> user = messageServer.users.stream().filter(u-> u.login.equals(login)
                && u.password.equals(password)).findFirst();
        if(user.isPresent()){
            this.isAuthorized = true;
            this.user = user.get();
            Frame frame = new Frame(SERVER_LOGIN, "You are logged in");
            frame.userId = this.user.getId();
            sendFrame(this.user,out, frame,encryptor);
        }
        else{
            Frame frame = new Frame(SERVER_LOGIN_FAILED, "NO SUCH USER");
            sendFrame(new User("","", publicKey), out, frame,encryptor);
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
            sendFrame(this.user, out, frame,encryptor);
            return;
        }
        Message m = new Message(user.getId(), chatId, text, textBytes);
        messageServer.messages.add(m);
        saveMessages();

        for(ClientThread socket : clientSockets){
            if(clientSocket != socket.clientSocket && chat.get().userIds.contains(socket.user.getId())){
                Frame frame = new Frame(SERVER_NEW_MESSAGE, text);
                frame.otherUserId = this.user.getId();
                frame.chatId = chatId;
                sendFrame(socket.user, socket.out, frame, encryptor);
            }
            else{
                Frame frame = new Frame(SERVER_MSG_CHAT, "Message Was sent");
                sendFrame(user, socket.out, frame, encryptor);
            }
        }
    }
    public List<Message> getAllMessagesFromChat(String chatId){

        Optional<Chat> chat = getChatById(chatId);
        if(chat.isEmpty()){
            Frame frame = new Frame(SERVER_UNKNOWN,"NO SUCH CHAT");
            sendFrame(user, out, frame,encryptor);
            return new ArrayList<>();
        }
        List<ClientThread> clientSockets = messageServer.clients;
        if(!chat.get().userIds.contains(user.getId())){
            Frame frame = new Frame(SERVER_SECURITY,"YOU ARE NOT ALLOWED TO WRITE IN THIS CHAT" );
            sendFrame(user,out,frame, encryptor);
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
        for(var chat : messageServer.chats){
            System.out.println("Other chats" + chat.getId());
        }

        if(c.isPresent()){
            c.get().userIds.add(this.user.getId());

            Frame frame = new Frame(SERVER_JOINED_CHAT, "You are added to a chat");
            sendFrame(this.user, out, frame,encryptor);
        }
        else{
            Frame frame = new Frame(SERVER_UNKNOWN, "NO SUCH CHAT");
            sendFrame(this.user, out, frame,encryptor);
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
        sendFrame(this.user, out, frame,encryptor);

        joinChat(chat.getId());
    }
}
