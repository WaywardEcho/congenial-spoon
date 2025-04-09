package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network

//runnable allows for multiple iterations to be running at the same time
class ClientHandler implements Runnable {
	public Socket socket;             // client socket
	private PrintWriter out;           // output stream to send data to the client (send message)
	private BufferedReader in;         // input stream to receive data (listen for message) from client
	private String username;           // client's username	
    public boolean loggedIn = false;
    
    public ClientHandler(Socket socket) {
        this.socket = socket; // store the client socket when the handler is created
    }
    
    @Override
    public void run() {
        try {
        	// initialize the input stream to read IN data from the client
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // initialize the output stream to send data OUT from the client with auto-flush enabled. auto flush is necessary to keep each print situtation on the correct lines
            out = new PrintWriter(socket.getOutputStream(), true);
                        
            while (!loggedIn) { //while actively using port
            	out.println("Enter your username: ");
            	username = in.readLine();
            	
            	 //if username exists, enter password
            	if (ChatServer.isUsernameTaken(username)) {
                    out.println("Username found. Enter your password: ");
                    String password = in.readLine();

                    //is it actually user or is someone else trying to hack into account
                    if (ChatServer.isValidPassword(username, password)) {
                       //make welcome message a function and insert here
                    	out.println("Login successful");
                        loggedIn = true;  // exit loop after "logging" in successfully
                    } else {
                        out.println("Incorrect password. Returning to username prompt...");//aware that user has to reenter user name to get to password part
//                        continue;
                    }
                } else {
                    out.println("Username not found. Would you like to create a new account? (yes/no)");
                    String response = in.readLine(); //prompt user to create account bc username didnt exist
                    out.println("DEBUG: User response for account creation: " + response); // Debug log to check user input


                    if (response != null && response.equalsIgnoreCase("yes")) {
                    	createNewAccount();           	
                        out.println("Enter a new password: ");
                        String newPassword = in.readLine();
                        System.out.println("DEBUG: New password entered: " + newPassword); // Debug log for new password
                        ChatServer.addUser(username, newPassword);
                        out.println("Login successful");
                        loggedIn = true;  // exit loop if account gets created
                    } else { //this should prevent users from entering incorrect password and being assigned username they dont deserve! identity fraud! 
                    		out.println("Returning to username prompt...");
//                    		continue; // restart loop. ask for username again
                    			
                    }
                }
            }
            
            
            // welcome message and list of other online users
            sendWelcomeMessage();
            
            System.out.println(username + " has joined the chat!"); //log to server
            ChatServer.broadcast(username + " has joined the chat!", this); //announce to all other users
            
            handleMessages();
            
        } catch (IOException e) {
            if (!ChatServer.isShuttingDown()) { // prevent errors from printing after shutdown
                System.out.println("Error handling client " + username + ": " + e.getMessage());
            }
        } finally {
            // In the finally block, ensure all resources are closed and the client is removed
        	cleanup();
        }
    }
    
    public void createNewAccount() throws IOException {
        while (!ChatServer.isValidUsername(username) || ChatServer.isUsernameTaken(username)) {
            out.println("Invalid username. (Rules: <32 characters, no spaces, no ^-: characters) Please try again.");
            out.println("Enter your username: ");
            username = in.readLine();
        }// loops until they enter a valid username that is not already taken

        out.println("Enter a new password: ");
        String newPassword = in.readLine();
        ChatServer.addUser(username, newPassword);
        out.println("Login successful");
        loggedIn = true;
    }
    
    public void sendWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome ").append(username).append("! \n");
        sb.append("Currently connected users: ");
        boolean first = true;
        for (ClientHandler client : ChatServer.getClients()) {
            if (client != this && client.getUsername() != null) {
                if (!first) {
                    sb.append(", ");//adds to list if it's not empty
                }
                sb.append(client.getUsername());
                first = false;
            }
        }
        if (first) { // no other user
        	sb.append("none");
        }
        // send the welcome message (only to this user)
        sendMessage(sb.toString());
        
    }
    
    public void handleMessages() throws IOException {
    	// accept constant messages from user
        String message;
        while ((message = in.readLine()) != null) { // exit allows user to disconnect. could change this to a different command but it's a placeholder for now
        	//i think we should change "exit" to "/exit", so if feels more like a command and allows the user to use the word exit in chat without getting booted. plus we could add "/help" for users to use while freaking out from our malware, but it just makes the symptoms worse or smth
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }
            // start line w/username and broadcast the message
            if (loggedIn) {
            	ChatServer.broadcast(username + ": " + message, this);
            }
        }
    }
    
    public void cleanup() {    
		try {
			if (socket != null) {
				socket.close();
			}
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			System.out.println("Error closing resources for " + username + ": " + e.getMessage());
		}
		// remove this client handler from the server's client list
		ChatServer.removeClient(this);
    }

    // to send a message to this client
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);  // send the message
            out.flush();           // flush to ensure it is sent immediately.
            //with all the inputs this program takes, flush becomes our bestie to make sure it's actually understanding wtf is going on
        }
    }
    
    // exactly what it says
    public String getUsername() {
        return username;
    }
}
