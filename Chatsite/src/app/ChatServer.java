package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network
import java.util.*;    // lists
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list

public class ChatServer {
    private static final int PORT = 9002; // port number on which the server listens
    // list of connected users
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT); //start up message, can be changed later
            
            // continuously accept new client connections
            while (true) {
            	Socket clientSocket = serverSocket.accept();   // accepts a  connection
				ClientHandler clientHandler = new ClientHandler(clientSocket); // create a handler for the client (does most of the client work)
				clients.add(clientHandler);                // add the handler to the list of clients
				new Thread(clientHandler).start();         // start a new thread for this client (where their messages will appear)
             }
            
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
    
    // broadcast a message to all clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting: " + message); // debug output on the server console. can be removed whenever, just used to make sure messages are getting sent
        // loop through all clients
        for (ClientHandler client : clients) {
            // skip the sender (so they don't get their own messages)
            if (client != sender) {
                // use the public sendMessage() method to send the message
                client.sendMessage(message);
            }
        }
    }
    
    // remove a client from the list
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
    
    // returns a list of the clients
    public static List<ClientHandler> getClients() {
        return clients;
    }
}
