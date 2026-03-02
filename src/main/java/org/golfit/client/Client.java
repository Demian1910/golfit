package org.golfit.client;

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
        System.out.println("Client started");
    }
}
