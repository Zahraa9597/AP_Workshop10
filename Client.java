import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 4321);
            System.out.println("********** WELCOME TO MY CHATROOM! **********");

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            System.out.print("enter your username: ");
            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();
            System.out.println(username + " joined Chatroom!");

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = inputStream.readUTF();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            while (true) {
                String message = scanner.nextLine();
                outputStream.writeUTF(message);
                if (message.equalsIgnoreCase("#exit")) {
                    System.out.println(username + "left the chatroom");
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
