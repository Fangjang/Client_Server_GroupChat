package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{
    //Arraylist of clients
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    //Socket
    private Socket socket = null;

    //String
    String userName;

    //IO stream
    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    //Colors
    //Color codes
    String RESET = "\u001B[0m";
    String GREEN = "\u001B[32m";
    String YELLOW = "\u001B[33m";

    ClientHandler (Socket socket) {
        this.socket = socket;

        initSocket();
        ioCycle();
    }

    //Function that initialized fields
    private void initSocket() {
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            userName = bufferedReader.readLine();

            String newUserInfo = userName + " has entered the chat.";
            System.out.println(newUserInfo);

            for(ClientHandler clients : ClientHandler.clientHandlers) {
                clients.bufferedWriter.write(newUserInfo);
                clients.bufferedWriter.newLine();
                clients.bufferedWriter.flush();
            }

        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void ioCycle() {
        Thread receiveThread = new Thread(() -> {
           try {
               //Reading msg
               String msgToReceive;

               while(socket.isConnected() && (msgToReceive = bufferedReader.readLine())!= null ) {
                   //Exit on 'exit'
                   if(msgToReceive.equalsIgnoreCase("bye")) {
                       break;
                   }

                   if(msgToReceive.equalsIgnoreCase("@online")) {
                       bufferedWriter.write("\t" + clientHandlers.size() + " online");
                       bufferedWriter.newLine();
                       bufferedWriter.flush();

                       int userCount = 1;
                       for(ClientHandler clients : clientHandlers) {
                           bufferedWriter.write(userCount + ". " + clients.userName);
                           bufferedWriter.newLine();
                           bufferedWriter.flush();

                           ++userCount;
                       }
                       continue;
                   }


                   System.out.println(userName + ": " + msgToReceive);
                   //broadcast the msg
                   for(ClientHandler clients : ClientHandler.clientHandlers) {
                       if(clients.socket != this.socket) {
                           String toSendMsg = GREEN + userName + ": " + msgToReceive + RESET;
                           clients.bufferedWriter.write(toSendMsg);
                           clients.bufferedWriter.newLine();
                           clients.bufferedWriter.flush();
                       }
                   }
               }
               //Closing conn
               closeConnection();

           } catch (Exception e) {
               System.out.println("Error Receiving Msg from" + userName + ";" + e.getMessage());
               e.printStackTrace();
           }
        });
        receiveThread.start();
    }

    //send msg to this.socket
    public void sendMsg(String msgToSend) {
        try {
            bufferedWriter.write(msgToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        try {
            //Broadcast leaving notification
            for(ClientHandler clients : ClientHandler.clientHandlers) {
                if(clients.socket != this.socket) {
                    String toSendMsg = userName + " has left.";
                    clients.bufferedWriter.write(toSendMsg);
                    clients.bufferedWriter.newLine();
                    clients.bufferedWriter.flush();
                }
            }

            //CLosing streeams and sockets
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (inputStreamReader != null) inputStreamReader.close();
            if (outputStreamWriter != null) outputStreamWriter.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error Closing Connection: " + e.getMessage());
            e.printStackTrace();
        }
        clientHandlers.remove(this);
        System.out.println(userName + " has left.");
    }
}
