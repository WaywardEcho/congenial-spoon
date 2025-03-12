package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network
import java.util.*;    // lists
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list

public class ChatServer {
    private static final int PORT = 8080; // port number on which the server listens
    // list of connected users
    private static final String USERNAMES_FILE = "storedUsernames.txt"; 
    private static Set<String> storedUsernames = new HashSet<>(); // hashset (every item is unique) to store usernames
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
    
    // Load usernames into storedUsernames set
    private static void loadUsernames() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAMES_FILE))) {
            String username;
            while ((username = reader.readLine()) != null) {
                storedUsernames.add(username.trim());
            }
            //to delete
            System.out.println("Existing usernames: " + storedUsernames);
        } catch (IOException e) {
            System.err.println("Error loading usernames: " + e.getMessage());
        }
    }

    // Check if a username is valid
    public static boolean isUsernameValid(String username) {
        return storedUsernames.contains(username);
        System.out.println("Checking username: " + username);
        System.out.println("Usernames in system: " + ChatServer.isUsernameValid()); // Assuming you have such a method)
    }
    
    //add new username 
    public static void addUsername(String username) {
        if (username == null || username.trim().isEmpty()) { //username cant be empty
            System.err.println("Invalid username: cannot be null or empty."); 
            //TO DO: prevent users with usernames starting with "^, :,  , "" etc. 
            return;
        }

        //add to hashset
        storedUsernames.add(username);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERNAMES_FILE, true))) {
            writer.write(username);
            writer.newLine(); //write to hashset
            System.out.println("Added new username: " + username);
        } catch (IOException e) {
            System.err.println("Error adding username to file: " + e.getMessage());
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
    
    //needed?
    // returns a list of the clients
    public static List<ClientHandler> getClients() {
        return clients;
    }
}
