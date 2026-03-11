package org.golfit;

import  org.golfit.client.Client;
import org.golfit.graphics.Window;
import org.golfit.server.Server;

public class Main {
    public static void main(String[] args) {
        // Launches Game-Window, but main thread is blocked.
        Window.launch(Window.class, args);

        if(args.length == 0) {
            printUsage();
            return;
        }
        switch (args[0]) {
            case "server" -> {
                if (args.length < 2) {
                    printUsage();
                    return;
                }
                int port = Integer.parseInt(args[1]);
                new Server(port).start();
            }
            case "client" -> {
                if (args.length < 2) {
                    printUsage();
                    return;
                }
                String[] hostPort = args[1].split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                String username = args.length >= 3 ? args[2] : null;
                new Client(host, port, username).start();
            }

        }
    }



    private static void printUsage() {
        System.out.println("Usage: (client <hostaddress>:<port> [<username>] | server <port>)");
    }
}
