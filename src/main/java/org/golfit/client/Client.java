package org.golfit.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    final private String host;
    final private int port;
    private String username;


    public Client(String host, int port, String username ) {
        this.host = host;
        this.port = port;
        this.username = username;
    }
    public void start() {
        try (Socket socket = new Socket(host, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            writer.println("Hello from the client2!");
            System.out.println("Message sent to server");

            String response = reader.readLine();
            System.out.println("Response from server: " + response);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }


    // temporal test
    public static void main(String[] args) {
        String hostname = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            writer.println("Hello from the client2!");
            System.out.println("Message sent to server");

            String response = reader.readLine();
            System.out.println("Response from server: " + response);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
