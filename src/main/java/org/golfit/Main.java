package org.golfit;

import javafx.application.Application;
import org.golfit.client.App;
import org.golfit.server.Server;


import org.golfit.client.clientLogic.Client;

/**
 * Entry point for the GolfIt application. Starts either a server or client based on CLI arguments.
 */
public class Main  {
    /** The currently active client instance, accessible globally for GUI event wiring. */
    public static Client currentClient;

    /**
     * Parses CLI arguments and launches either a server or client.
     *
     * @param args {@code server <port>} or {@code client <host>:<port> [<username>]}
     * @throws Exception if server or client startup fails
     */
    public static void main(String[] args) throws Exception {

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
                String port = hostPort[1];
                String username = args.length >= 3 ? args[2] : System.getProperty("user.name");
                String[] argsFinal = new String[] {host, port, username};

                Application.launch(App.class, argsFinal);
//                currentClient = new Client(host, port, username);
//                currentClient.start();

//                org.golfit.client.game.GraphicInit.main(args);
            }
            default -> printUsage();
        }
    }

    /** Prints the expected command-line usage to stdout. */
    private static void printUsage() {
        System.out.println("Usage: (client <hostaddress>:<port> [<username>] | server <port>)");
    }


}