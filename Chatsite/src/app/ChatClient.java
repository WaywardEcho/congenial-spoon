public class ChatClient {
            
    // login system
    // read the prompt from the server and display it
    try{
        LoginStateMachine loginSM = new LoginStateMachine(in, out, inputConsole);
        if(loginSM.runLoginProcess()){
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


            // Main thread: read user input from user and send to server, to send to all other users
                // this is how you disconnect, by typing exit
            if (userInput.equalsIgnoreCase("exit")) {
                out.println("exit");
                cleanUp();
                break;
            }
            // clean up resources (close the socket and all streams) 
            System.out.println("Exiting...");
            out.println("exit");
        
        }
    
// error messages. def can be tweaked
} catch (UnknownHostException u) {
    // handle exception when the host is unknown
    System.out.println("Host unknown: " + u.getMessage());
} catch (IOException i) {
    // handle general I/O exceptions
    System.out.println("Unexpected exception: " + i.getMessage());
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
                System.out.print(username + ": ");
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
    String serverAddress = "192.168.36.112"; //Change this to the ip_address of the computer running the server
    int serverPort = 8000;
    new ChatClient(serverAddress, serverPort); //localhost/server. 
}
