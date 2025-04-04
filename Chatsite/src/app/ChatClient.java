package app;

import java.io.*;     // input/output
import java.net.*;    // socket and network



public class ChatClient {
	// vars to manage the socket and I/O streams
    private Socket socket = null;
    private BufferedReader inputConsole = null;  // reads user input from the console
    private PrintWriter out = null;              // sends data to the server
    private BufferedReader in = null;            // receives data from the server
    private String username;  					// saves the user's name
    private boolean loggedIn = false;
    
    public ChatClient(String address, int port) {
    	try {
            // connect to the server using the provided address and port
        	//will likely tweak this in order to increase user ease once we have a GUI going
            socket = new Socket(address, port);
            System.out.println("Connection Established!"); // message outputed to the user 
            
            
            // THIS IS THE BASIS OF BEING ABLE TO CREATE AND SEND MESSAGES
            // create/set the console input reader
            inputConsole = new BufferedReader(new InputStreamReader(System.in));
            // create/set the output stream to send data to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            // create/set the input stream to receive data from the server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String userInput;
            LoginStateMachine loginSM = new LoginStateMachine(in, out, inputConsole);
            
            // login system
            // read the prompt from the server and display it
            if(loginSM.runLoginProcess()){
            	username = loginSM.getUsername();
                // Login succeeded; proceed with starting the message listener thread, etc.
                loggedIn = true;
                startMessageListener();
                // Continue with user input loop for sending messages
            } else {
                // Login failed or connection closed
                cleanUp();
                System.exit(0);
            }



            if(loggedIn) {
            // start a thread to continuously read messages from the server
            	startMessageListener();
            	System.out.print(username + ": ");
	
	            // Main thread: read user input from user and send to server, to send to all other users
	            
	            while (true) {
	            	System.out.print(username + ": "); // display username prompt before input // "echo: " "megan: " "elise: "    	
	                userInput = inputConsole.readLine(); // read typed message
	
	                // checks if the connection to the server is still open
	                if (socket.isClosed() || !socket.isConnected()) {
	                    System.out.println("Disconnected from server. Exiting...");
	                    break; // breaks loop/disconnects if there's no connection
	                }// this is to prevent users from being able to send messages once the server closes
	                
	                // checks and skips empty messages (like blank enters)
	                if (userInput == null || userInput.trim().isEmpty()) {
	                    continue;
	                }
	
	                // this is how you disconnect, by typing exit
	                if (userInput.equalsIgnoreCase("exit")) {
	                    out.println("exit");
	                    cleanUp();
	                    break;
	                }
	
	                // send only the message (server will add the username)
	                out.println(userInput);
	            }
	            
            }
	
	        // clean up resources (close the socket and all streams) 
            System.out.println("Exiting...");
            out.println("exit");
            
        // error messages. def can be tweaked
        } catch (UnknownHostException u) {
            // handle exception when the host is unknown
            System.out.println("Host unknown: " + u.getMessage());
        } catch (IOException i) {
            // handle general I/O exceptions
            System.out.println("Unexpected exception: " + i.getMessage());
        }

    }

    public void startMessageListener() {
    	//this is the part that holds any of the messages together in a cohearant order. every tweak to this makes the username print as part of the input
    	//i got so sick of seeing "echo: echo:" and "willow: willow:"
        new Thread(() -> {
            try {
                String serverMsg;
                // continuously read messages from the server
                	//listening basically
                while ((serverMsg = in.readLine()) != null) {
                	
                		System.out.print("\r" + " ".repeat(50) + "\r");
                         //print the incoming message on its own line
                        System.out.println(serverMsg);
                            // reprint the prompt so the user knows it's their turn to type
//                        System.out.print(username + ": ");
                }
                
            } catch (IOException e) {
                System.out.print("Disconnected from server. Exiting...");
                cleanUp();
                System.exit(0); // Close client on disconnect
            }
        }).start();
    }

    public void cleanUp() {
    	try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (inputConsole != null) {
                inputConsole.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
    	String serverAddress = "192.168.37.211"; //Change this to the ip_address of the computer running the server
        int serverPort = 8000;
        new ChatClient(serverAddress, serverPort); //localhost/server. 
    }
    
}
