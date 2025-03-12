package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network
import java.util.*;    // lists
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list

public class ChatServer {
    private static final int PORT = 8080; // port number on which the server listens
    // list of connected users
    private static final String USERNAMES_FILE = "storedUsernames.txt"; 
    private static Map<String, String> storedUsernames = new HashMap<>(); // hashmap for key-value pairs (username: password)
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
    	loadUsernames(); //load usernames and passwords upon start
    	
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
    
    // Load usernames and passwords into storedUsernames.txt
    private static void loadUsernames() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAMES_FILE))) {
            String username;
            while ((username = reader.readLine()) != null) {
            	String[] parts = username.split(":", 2); //split into username part and password part
            	 if (parts.length == 2) {
            		 storedUsernames.put(parts[0].trim(), parts[1].trim());
            		 System.out.println("Loaded user: "+ parts[0]); //debug
            	 }
            }
            // System.out.println("Total loaded usernames: " + storedUsernames.size()); // debug
        
        } catch (IOException e) {
             System.err.println("Error loading usernames: " + e.getMessage());
         }
     }

    // validate username and password
    public static boolean isUserValid(String username, String password) {
        return storedUsernames.containsKey(username) && storedUsernames.get(username).equals(password);
    }
    
    //check if username exists
    public static boolean isUsernameTaken(String username) {
    	return storedUsernames.containsKey(username);
    }
    
    //add new username with password
    public static void addUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.err.println("Invalid username or password.");
            return;
        }
        
       storedUsernames.put(username, password); //add to hashmap
       
       //write to hashmap
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERNAMES_FILE, true))) {
            writer.write(username + ":" + password);
            writer.newLine(); 
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
