import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private ServerSocket serverSocket;
    private Map<Socket, String> clientsMap;

    public ChatRoom(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientsMap = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Chat room server started. Waiting for clients to connect...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                handleNewClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNewClient(Socket clientSocket) {
        try {
            String username = getUsernameFromClient(clientSocket);
            clientsMap.put(clientSocket, username);
            broadcastMessage(username + " has joined the chat room.");
            Thread thread = new Thread(new ClientHandler(clientSocket, this), "Client Handler");
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUsernameFromClient(Socket clientSocket) throws IOException {
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
        outputStream.writeUTF("Please enter a username: ");
        return inputStream.readUTF();
    }

    public void broadcastMessage(String message) {
        for (Map.Entry<Socket, String> entry : clientsMap.entrySet()) {
            Socket socket = entry.getKey();
            String username = entry.getValue();
            sendMessageToClient(socket, username, message);
        }
    }

    private void sendMessageToClient(Socket clientSocket, String username, String message) {
        try {
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeUTF("[" + username + "]: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientDisconnection(Socket clientSocket) {
        String username = clientsMap.remove(clientSocket);
        broadcastMessage(username + " has left the chat room.");
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ChatRoom chatRoom;

        public ClientHandler(Socket clientSocket, ChatRoom chatRoom) {
            this.clientSocket = clientSocket;
            this.chatRoom = chatRoom;
        }

        public void run() {
            try {
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                while (true) {
                    String message = inputStream.readUTF();
                    if (message.equalsIgnoreCase("OVER")) {
                        break;
                    }
                    chatRoom.broadcastMessage(message);
                }
            } catch (IOException e) {
                chatRoom.handleClientDisconnection(clientSocket);
            }
        }
    }


}