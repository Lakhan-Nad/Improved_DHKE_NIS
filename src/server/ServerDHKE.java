package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerDHKE {
    private static String id;
    private static ServerSocket server;

    private ServerDHKE() {
        // Empty Constructor
    }

    private static void handleArgs(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected Server Id and Port Number");
            System.exit(1);
        }
        id = args[0];
        server = null;
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Port is not a valid number");
            return;
        }
        // try creating a ServerSocket
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleConnection(Socket socket) {
        System.out.println("Connection request from: " + socket.getInetAddress());
        Thread th = new ClientThread(socket, id);
        try {
            th.start();
            th.join();
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        handleArgs(args);
        if (server == null) {
            System.out.println("Unable to initiate a server");
            System.exit(1);
        }
        // server is ready
        int connections = 2;
        AuthManager.load();
        while (connections > 0) {
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (Exception e) {
                System.out.println("Unable to accept request");
            }
            if (socket != null) {
                handleConnection(socket);
                connections--;
            } else {
                System.out.println("Connection Error");
            }
        }
        AuthManager.syncWithFile();
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }
    }
}
