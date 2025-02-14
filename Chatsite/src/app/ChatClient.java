package app;

import java.io.*;
import java.net.*;

public class ChatClient {
  private Socket socket = null;
  private BufferedReader inputConsole = null;
  private PrintWriter out = null;
  private BufferedReader in = null;

  public ChatClient(String address, int port) {
      try {
          socket = new Socket(address, port);
          System.out.println("Connected to the chat server");

          inputConsole = new BufferedReader(new InputStreamReader(System.in));
          out = new PrintWriter(socket.getOutputStream(), true);
          in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

          String line = "";
          while (!line.equals("exit")) {
              line = inputConsole.readLine();
              out.println(line);
              System.out.println(in.readLine());
          }

          socket.close();
          inputConsole.close();
          out.close();
      } catch (UnknownHostException u) {
          System.out.println("Host unknown: " + u.getMessage());
      } catch (IOException i) {
          System.out.println("Unexpected exception: " + i.getMessage());
      }
  }

  public static void main(String args[]) {
	  //edit this to allow a user to be chosen
      ChatClient client = new ChatClient("127.0.0.1", 5000);
  }
}