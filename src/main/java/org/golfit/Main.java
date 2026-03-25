package org.golfit;

import javafx.application.Application;
import org.golfit.client.Client;
import org.golfit.server.Server;
import org.golfit.graphics.Window;

/**
 * Entry point for the GolfIt application. Starts either a server or client based on CLI arguments.
 */
public class Main  {
    public static Client currentClient;

    public static void main(String[] args) throws Exception {
        /*
        To display the window, uncomment and run Main
        try {
        Application.launch(Window.class, args);
        } catch (RuntimeException e) {

            throw new RuntimeException(e);
        }

         */
        if (args.length == 0) {
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
                String username = args.length >= 3 ? args[2] : "Player" + (int)(Math.random() * 10000);

                currentClient = new Client(host, port, username);
                currentClient.start();

                org.golfit.client.game.GraphicInit.main(args);
            }
            default -> printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: (client <hostaddress>:<port> [<username>] | server <port>)");
    }


}