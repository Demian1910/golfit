package org.golfit.server.ServerInit;

/** Handles server startup output including the ASCII art banner. */
public class ServerInit {
    /** Prints the server welcome banner and port information to the console. */
    public static void serverInit(int port) {
        System.out.println("Server Initialized");
        System.out.println("Welcome to Golfit");
        System.out.println("""
            
             ██████╗  ██████╗ ██╗     ███████╗██╗████████╗
            ██╔════╝ ██╔═══██╗██║     ██╔════╝██║╚══██╔══╝
            ██║  ███╗██║   ██║██║     █████╗  ██║   ██║   
            ██║   ██║██║   ██║██║     ██╔══╝  ██║   ██║   
            ╚██████╔╝╚██████╔╝███████╗██║     ██║   ██║   
             ╚═════╝  ╚═════╝ ╚══════╝╚═╝     ╚═╝   ╚═╝   
                        ⛳ Mini Golf Server ⛳
            """);
        System.out.println("Server is running on port " + port);
        System.out.println("Type '/help' for a list of commands");
    }
}
