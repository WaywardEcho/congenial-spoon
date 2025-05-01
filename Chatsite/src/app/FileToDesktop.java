package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileToDesktop {

  public static void main(String[] args) {
    String desktopPath;

    // get the user's home directory
    String userHome = System.getProperty("user.home");
    desktopPath = userHome + "/Desktop/";

    // create a file obj for the desktop file
    File desktopFile = new File(desktopPath);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(desktopFile, true))) {
        writer.write("Hello, world!");// Hello, world! should be replaced by file contents.
    } catch (IOException e) { //we'd want this part to only send to the server
        System.err.println("An error occurred while writing to the file: " + e.getMessage());
        e.printStackTrace();
    }
  }
}