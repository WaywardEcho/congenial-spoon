package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network

//runnable allows for multiple iterations to be running at the same time
class ClientHandler implements Runnable {
	public Socket socket;             // client socket
	private PrintWriter out;           // output stream to send data to the client (send message)
	private BufferedReader in;         // input stream to receive data (listen for message) from client
	private String username;           // client's username
    
    public ClientHandler(Socket socket) {
        this.socket = socket; // store the client socket when the handler is created
        					  // we do not want to lose this, otherwise nothing works
    }
    
    @Override
    public void run() {
        try {
        	// initialize the input stream to read IN data from the client
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // initialize the output stream to send data OUT from the client with auto-flush enabled. auto flush is necessary to keep each print situtation on the correct lines
            out = new PrintWriter(socket.getOutputStream(), true);
            
            boolean loggedIn = false;
            while (!loggedIn) { //while actively using port
            	out.println("Enter your username: ");
            	username = in.readLine();
            	 
            	 //if username exists, enter password
            	if (ChatServer.isUsernameTaken(username)) {
                    out.println("Username found. Enter your password: ");
                    String password = in.readLine();

                    //is it actually user or is someone else trying to hack into account
                    if (ChatServer.isUserValid(username, password)) {
                        out.println("Login successful. Welcome, " + username + "!");
                        loggedIn = true;  // exit loop after "logging" in successfully
                    } else {
                        out.println("Incorrect password. Try again."); //aware that user has to reenter user name to get to password part
                    }
                } else {
                    out.println("Username not found. Would you like to create a new account? (yes/no)");
                    String response = in.readLine(); //prompt user to create account bc username didnt exist

                    if (response != null && response.equalsIgnoreCase("yes")) { 
                        out.println("Enter a new password: ");
                        String newPassword = in.readLine();
                        ChatServer.addUser(username, newPassword);
                        out.println("Account created successfully. Welcome, " + username + "!");
                        loggedIn = true;  // exit loop if account gets created
                    }
                }
            }
            //TO FIX: current username bug: user says they are A, fails password, system refers to as A ,
            //A decides to create account under B, system still referes to them as A
            
            
            // welcome message and list of other online users
            //should welcome message be a function? -mm
            StringBuilder sb = new StringBuilder();
            sb.append("Welcome ").append(username).append("! \n");
            sb.append("Currently connected users: ");
            boolean first = true;
            for (ClientHandler client : ChatServer.getClients()) { //loops through each user
                if (client != this && client.getUsername() != null) {
                    if (!first) {
                        sb.append(", "); //adds to list if it's not empty
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

            
            System.out.println(username + " has joined the chat!"); //log to server
            ChatServer.broadcast(username + " has joined the chat!", this); //announce to all other users
            
            
            
            
            // accept constant messages from user
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) { //exit allows user to disconnect. could change this to a different command but it's a placeholder for now
                    break;
                }
                // start line w/username and broadcast the message
                ChatServer.broadcast(username + ": " + message, this);
            }
        } catch (IOException e) {
            if (!ChatServer.isShuttingDown()) { // prevent errors from printing after shutdown
                System.out.println("Error handling client " + username + ": " + e.getMessage());
            }
        } finally {
            // In the finally block, ensure all resources are closed and the client is removed
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
    }

    // to send a message to this client
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);  // send the message
            out.flush();           // flush to ensure it is sent immediately. //this solves so many problems but honestly I'm so tired I can't remember specifics
        }
    }
    
    // exactly what it says
    public String getUsername() {
        return username;
    }

}
