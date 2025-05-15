package Client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client extends Thread{
    //Port & Address
    String address;
    int port;

    //Socket
    Socket socket = null;

    //IO Streams
    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    //User name
    String userName;

    //Color codes
    String RESET = "\u001B[0m";
    String GREEN = "\u001B[32m";
    String YELLOW = "\u001B[33m";

    Client(String address, int port, String userName) {
        this.address = address;
        this.port = port;
        this.userName = userName;

        initClient();
    }

    private void initClient() {
        try {
            socket = new Socket(address, port);


            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            bufferedWriter.write(this.userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            System.out.println("Connected to server Successfully");

        } catch (RuntimeException | IOException runtimeException) {
            System.out.println(runtimeException.getMessage());
            runtimeException.printStackTrace();
        }
    }

    public void ioCycle() {
        Scanner scanner = new Scanner(System.in);

        //Receiving msg in a thread
        Thread receiveThread = new Thread (() -> {
            try {
                String msgToReceive;
                while((msgToReceive = bufferedReader.readLine()) != null) {
                    System.out.println(msgToReceive);
                }
            } catch (IOException ioException) {
                System.out.println("Socket Disconnected");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
        receiveThread.start();

        try {
            while(true) {
                String msgToSend = scanner.nextLine();
                System.out.println(GREEN + "You(Client): " + msgToSend + RESET);

                if(msgToSend.equalsIgnoreCase("bye")) {
                    break;
                }

                if(msgToSend.isEmpty() | msgToSend.isBlank()) {
                    continue;
                }

                bufferedWriter.write(msgToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            receiveThread.interrupt();
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            socket.close();
            inputStreamReader.close();
            outputStreamWriter.close();
            bufferedWriter.close();
            bufferedReader.close();
        } catch (RuntimeException | IOException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        } finally {
            System.out.println("Socket Closed");
        }
    }
}