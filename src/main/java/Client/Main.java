package Client;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String userName = "";

        System.out.println("Enter your Username for chat;");

        try {
            while (userName.isBlank()){
                userName = scanner.nextLine();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        Client client = new Client("127.0.0.1", 5000, userName);
        client.ioCycle();
    }
}
