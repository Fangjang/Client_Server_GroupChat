package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread{
    private int port;

    //Socket
    Socket socket = null    ;
    ServerSocket serverSocket = null;

    //Thread
    Thread awaitClients = null;

    //Input & Output streams
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    Server(int port) {
        //Set address and port
        this.port = port;

        serverInit();
    }

    private void serverInit() {
        try {
            //Configure sockets
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            System.out.println("Client Connected");

            ClientHandler.clientHandlers.add(new ClientHandler(socket));

//            //Configure IO stream
//            inputStreamReader = new InputStreamReader(socket.getInputStream());
//            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//            bufferedReader = new BufferedReader(inputStreamReader);
//            bufferedWriter = new BufferedWriter(outputStreamWriter);

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ioCycle() {
        Scanner scanner = new Scanner(System.in);

        awaitClients = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Socket sckt = serverSocket.accept();
                    ClientHandler.clientHandlers.add(new ClientHandler(sckt));
                } catch (IOException e) {
                    System.out.println("Error connecting to client: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        awaitClients.start();

//        new Thread(() -> {
//            try {
//                String msgToReceive;
//                while((msgToReceive = bufferedReader.readLine()) != null) {
//                    System.out.println("Client: " + msgToReceive);
//                }
//            } catch (IOException e) {
//                System.out.println("IOException: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }).start();
//
//
        try {
            while(true) {
                //Enter msg to send
                String toSendMsg = scanner.nextLine();
                System.out.println("Server: " + toSendMsg);

                //Exit if entered "exit"
                if(toSendMsg.equalsIgnoreCase("bye")) {
                    break;
                }

                if(toSendMsg.isBlank()) {
                    continue;
                }

                toSendMsg = "Server: " + toSendMsg;
                for(ClientHandler clients : ClientHandler.clientHandlers) {
                    clients.sendMsg(toSendMsg);
                }
            }
        } catch (Exception e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            // Interrupt thread safely
            if (awaitClients != null && awaitClients.isAlive()) {
                awaitClients.interrupt();
            }

            // Close all resources
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            if (inputStreamReader != null) inputStreamReader.close();
            if (outputStreamWriter != null) outputStreamWriter.close();
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }
}