package org.golfit.server;

public class Server {
    final private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Server started");
    }
}
