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
         
            
            // login system
            // read the prompt from the server and display it
            while (true) {
            	
                // read and display the server's prompt
                String prompt = in.readLine();
                if (prompt == null) {
                    System.out.println("Connection closed by server.");
                    break;
                }
                
                // print the prompt received from the server
                System.out.print(prompt); // expected to print "Enter your username: ", "Username not found. Would you like to create a new account? (yes/no)"
                
                System.out.print(prompt.contains("Would you like to create a new account"));

                // if the prompt asks about creating a new account, read user response
                if (prompt.contains("Would you like to create a new account")) {
                	System.out.print("DEBUG: Waiting for user response... ");  // Debug print
                	String accountChoice = inputConsole.readLine().trim();
                	System.out.println("DEBUG: User entered -> " + accountChoice);
                	
                	if (accountChoice == null || accountChoice.trim().isEmpty()) {
                        System.out.println("DEBUG: No input provided. Returning to username prompt.");
                        continue;
                    }
                	out.println(accountChoice);  // Send response to server
                    System.out.println("DEBUG: Sent '" + accountChoice + "' to server.");

                    if ("yes".equalsIgnoreCase(accountChoice)) {
                        // Proceed with account creation
                        System.out.println("DEBUG: Creating new account...");
                    } else {
                        // Handle invalid input or return to login
                        System.out.println("DEBUG: Invalid input. Returning to login prompt.");
                        continue;
                    }

                    
                    // Read the next response to prevent skipping
                    String followUpResponse = in.readLine();
                    if (followUpResponse != null) {
                        System.out.println("DEBUG: Server follow-up response: " + followUpResponse);
                    } else {
                        System.out.println("DEBUG: No follow-up response from server.");
                    }
                    continue; // Restart loop so user is properly prompted again
                }

                // otherwise, assume it's asking for username or password
                String userInput = inputConsole.readLine().trim();
                
                // check for null or "exit" before sending to the server
                if (userInput.isEmpty() || "exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Exiting...");
                    out.println("exit");
                    socket.close();
                    System.exit(0);
                }

                out.println(userInput);

                // read and process the next response
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Connection closed by server.");
                    break;
                }

                System.out.println(response);

                if (response.contains("Enter your password")) {
            		// now read the password since the server is prompting for it
                    String password = inputConsole.readLine().trim();
                    if (password.isEmpty()) {
                        System.out.println("Password cannot be empty. Returning to username prompt.");
                        continue;
                    }
                    out.println(password);

                    // Process login result
                    String loginResult = in.readLine(); // read login success/failure
                    System.out.println(loginResult);

                    if (loginResult.contains("Login successful")) {
                        username = userInput;
                        break; // Exit loop since login succeeded
                    }
                }            		
            }


            // start a thread to continuously read messages from the server
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
                            System.out.print(username + ": ");
                    	}
                    
                } catch (IOException e) {
                    System.out.print("Disconnected from server. Exiting...");
                    System.exit(0); // Close client on disconnect
                }
            }).start();


            // Main thread: read user input from user and send to server, to send to all other users
            String userInput;
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
                    break;
                }

                // send only the message (server will add the username)
                out.println(userInput);
            }

            // clean up resources (close the socket and all streams) 
            socket.close();
            inputConsole.close();
            out.close();
            in.close();
        // error messages. def can be tweaked
        } catch (UnknownHostException u) {
            // handle exception when the host is unknown
            System.out.println("Host unknown: " + u.getMessage());
        } catch (IOException i) {
            // handle general I/O exceptions
            System.out.println("Unexpected exception: " + i.getMessage());
        }
    }

    public static void main(String[] args) {
    	String serverAddress = "192.168.36.112"; //Change this to the ip_address of the computer running the server
        int serverPort = 8000;
        new ChatClient(serverAddress, serverPort); //localhost/server. 
    }
    
}
