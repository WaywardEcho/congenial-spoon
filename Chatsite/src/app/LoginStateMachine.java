package app;

import java.io.*;

public class LoginStateMachine {
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader console;
    
    public String username;
    
    public LoginStateMachine(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
        this.console = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public boolean runLoginProcess() throws IOException {
        LoginState currentState = LoginState.REQUEST_USERNAME;
        while (currentState != LoginState.DONE) {
            switch (currentState) {
                case REQUEST_USERNAME:
                    // Read the prompt from server (e.g., "Enter your username: ")
                    String prompt = in.readLine();
                    if(prompt == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.print(prompt);
                    System.out.flush();
                    this.username = console.readLine().trim();
                    if(this.username.isEmpty() || "exit".equalsIgnoreCase(username)) {
                        System.out.println("Exiting...");
                        out.println("exit");
                        return false;
                    }
                    out.println(this.username);
                    out.flush();
                    currentState = LoginState.RECEIVE_USERNAME_RESPONSE;
                    break;
                case RECEIVE_USERNAME_RESPONSE:
                    String response = in.readLine();
                    if(response == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.println(response);
                    currentState = currentState.next(response);
                    break;
                case REQUEST_PASSWORD:
                    // Server should now prompt for password.
                    String passPrompt = in.readLine();
                    if(passPrompt == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.print(passPrompt);
                    System.out.flush();
                    String password = console.readLine().trim();
                    if(password.isEmpty()) {
                        System.out.println("Password cannot be empty.");
                        continue;
                    }
                    out.println(password);
                    out.flush();
                    currentState = LoginState.RECEIVE_PASSWORD_RESPONSE;
                    break;
                case RECEIVE_PASSWORD_RESPONSE:
                    String loginResult = in.readLine();
                    if(loginResult == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.println(loginResult);
                    currentState = currentState.next(loginResult);
                    break;
                case ACCOUNT_CREATION:
                    // If username not found, server asks if you want to create a new account.
                    String accountPrompt = in.readLine();
                    if(accountPrompt == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.print(accountPrompt);
                    System.out.flush();
                    String decision = console.readLine().trim();
                    out.println(decision);
                    out.flush();
                    String acctResponse = in.readLine();
                    if(acctResponse == null) {
                        System.out.println("Connection closed by server.");
                        return false;
                    }
                    System.out.println(acctResponse);
                    currentState = currentState.next(acctResponse);
                    break;
                default:
                    break;
            }
        }
        // If DONE, login succeeded.
        return true;
    }
    
    public String getUsername() {
    	return this.username;
    }
}
