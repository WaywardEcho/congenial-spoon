package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network
import java.util.*;    // lists
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list

//hashing imports vvvv
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class ChatServer {
    private static final int PORT = 8000; // port number on which the server listens
    private static final String USERNAMES_FILE = "storedUsernames.txt"; // list of logins
    private static Map<String, String> storedUsernames = new HashMap<>(); // hashmap for key-value pairs (username: password)
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>(); // list of connected users
    private static volatile boolean isShuttingDown = false; // if the server is shutting down
    
    public static void main(String[] args) {
    	loadUsernames(); //load usernames and passwords upon start
    	
    	//this allows for a clean close. no more messages once the server is closed, as well as sending a message to all users before they're disconnected
    	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    	    System.out.println("Server shutting down. Disconnecting all clients...");
    	    isShuttingDown = true; // update that shutdown is in progress
    	    
    	    // message all clients that the server is closing
    	    for (ClientHandler client : clients) {
    	        client.sendMessage("Server is shutting down. You will be disconnected.");
    	    }

    	    // 1 sec for messages to send before forcibly disconnecting clients
    	    try { Thread.sleep(1000); } catch (InterruptedException e) { }

    	    // close all client connections
    	    for (ClientHandler client : clients) {
    	        try {
    	            client.socket.close(); // close each client's socket
    	        } catch (IOException e) {
    	        	if (!isShuttingDown) { // prevent extra error messages once closing
    	                System.err.println("Error closing client socket: " + e.getMessage());
    	            }    	        }
    	    }

    	    clients.clear(); // remove all clients from the list
    	}));
    	
    	
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT); //start up message, can be changed later
            
            // continuously accept new client connections
            while (true) {
            	try {
	            	Socket clientSocket = serverSocket.accept();   // accepts a  connection
					ClientHandler clientHandler = new ClientHandler(clientSocket); // create a handler for the client (does most of the client work)
					clients.add(clientHandler);                // add the handler to the list of clients
					new Thread(clientHandler).start();         // start a new thread for this client (where their messages will appear)
            	} catch(IOException e) {
            		System.out.println("Error accepting client connection: " + e.getMessage()); //if for some reason users can't connect
            	}
            }
            
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
    
    
    // hashes a password using SHA-256 and returns the result as a hexadecimal string
	public static String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256"); // create a SHA-256 MessageDigest instance
			byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8)); // hash the password input as bytes using UTF-8 encoding
			
			StringBuilder hexString = new StringBuilder(); // convert hashed bytes to hexadecimal string
			for (byte b : hashBytes) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found.", e); // should never happen unless Java runtime is misconfigured, but just in case!
		}
	}
    
    // Load usernames and passwords into storedUsernames.txt
    private static void loadUsernames() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAMES_FILE))) {
            String textLine;
            while ((textLine = reader.readLine()) != null) {
            	String[] parts = textLine.split(":", 2); //split into username part and password part
            	 if (parts.length == 2) {
            		 storedUsernames.put(parts[0].trim(), parts[1].trim()); //adds username + password, trim removes white space
            		 System.out.println("Loaded user: "+ parts[0]); //debug
            	 }
            }
        
        } catch (IOException e) {
             System.err.println("Error loading usernames: " + e.getMessage());
         }
     }

    // validate username and password
    public static boolean isValidUsername(String username) {
    	if (username == null || username.trim().isEmpty()) {
            return false; // username cant be empty
        }
        
        if (username.length() > 32) {
            return false; // max length: 32 characters
        }

        if (username.contains(" ")) {
            return false; // No spaces allowed!
        }

        // check restricted first characters (cant start with ^,:,- or anything else you can think of later
        char firstChar = username.charAt(0);
        if (firstChar == '^' || firstChar == ':' || firstChar == '-') {
            return false;
        }

    	return true;
    	//return storedUsernames.containsKey(username) && storedUsernames.get(username).equals(password);
    
    }

    // validates that the entered password matches the stored hashed password
    public static boolean isValidPassword(String username, String password) {
    	if (password == null || password.trim().isEmpty()){
    		 return false; // username cant be empty
    	}
    	
        String storedHash = storedUsernames.get(username); // look up the stored hash for this username
        if (storedHash == null) {
        	return false;
        }
        
        String enteredHash = hashPassword(password); // hash the entered password for comparison

        return storedHash.equals(enteredHash); // compare the hashes
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
    
       String hashed = hashPassword(password); // hash the plain-text password before storing
       storedUsernames.put(username, hashed); //add to hashmap
       
       //write to hashmap
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERNAMES_FILE, true))) {
            writer.write(username + ":" + hashed); //write value
            writer.newLine(); 
            System.out.println("Added new username: " + username); //displays username
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

    // returns a list of the clients (needed to know who is online when joining)
    public static List<ClientHandler> getClients() {
        return clients;
    }
    
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
    
}
