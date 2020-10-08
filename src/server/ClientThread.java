package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;

public final class ClientThread extends Thread {
    private final String serverId;
    private final Socket socket;
    private BigInteger sessionKey;
    ClientData client;
    DataInputStream in = null;
    DataOutputStream out = null;

    ClientThread(Socket socket, String id) {
        this.serverId = id;
        this.socket = socket;
    }

    private void establishIO() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println("Unable to close Input Stream");
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println("Unable to close Output Stream");
            }
        }
        try {
            if (!socket.isInputShutdown())
                in = new DataInputStream(socket.getInputStream());
            if (!socket.isInputShutdown())
                out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Unable to open communication streams");
            return;
        }
        if (in == null || out == null) {
            System.out.println("Communication Channels Unavailable");
        }
    }

    public void run() {
        establishIO();
        keyExchange();
        //communicate();
        close();
    }

    private void close() {
        try {
            if (socket != null)
                socket.close();
            if (out != null)
                out.close();
            if (in != null)
                in.close();
        } catch (Exception e) {
            System.out.println("Unable to Close the Connection");
        }
    }

    private void keyExchange() {
        if (in == null || out == null) {
            establishIO();
        }
        String[] clientInfo;
        try {
            clientInfo = in.readUTF().split("\\s+");
        } catch (IOException e) {
            System.out.println("Unable to read Data");
            return;
        }
        BigInteger xplust;
        if (clientInfo.length == 5) {
            BigInteger g, p, t;
            try {
                g = new BigInteger(clientInfo[1]);
                p = new BigInteger(clientInfo[2]);
                t = new BigInteger(clientInfo[3]);
                xplust = new BigInteger(clientInfo[4]);
            } catch (Exception e) {
                System.out.println("Invalid data found in File");
                return;
            }
            client = new ClientData(clientInfo[0], g, p, t.modInverse(p));
            AuthManager.addOrUpdate(client);
        } else if (clientInfo.length == 2) {
            client = AuthManager.find(clientInfo[0]);
            if (client == null) {
                System.out.println("Invalid key Exchange");
                return;
            }
            try {
                xplust = new BigInteger(clientInfo[1]);
            } catch (Exception e) {
                System.out.println("Invalid keys provided");
                return;
            }
        } else if (clientInfo.length == 3) {
            BigInteger t;
            client = AuthManager.find(clientInfo[0]);
            if (client == null) {
                System.out.println("Invalid key Exchange");
                return;
            }
            try {
                t = new BigInteger(clientInfo[2]);
                xplust = new BigInteger(clientInfo[1]);
            } catch (Exception e) {
                System.out.println("Invalid keys provided");
                return;
            }
            client.setTInv(t.modInverse(client.getP()));
            AuthManager.addOrUpdate(client);
        } else {
            System.out.println("Invalid Key Exchange");
            return;
        }
        BigInteger privateKey = calcPrivateSessionKey(client.getP());
        BigInteger publicKey = calcPublicKey(client, privateKey);
        sessionKey = calcSessionKey(calcClientPublicKey(xplust, client), privateKey, client.getP());

        System.out.println(sessionKey);

        // sending back keys
        StringBuilder buf = new StringBuilder();
        buf.append(serverId);
        buf.append(' ');
        buf.append(publicKey.toString());
        buf.append('\n');
        try {
            out.writeUTF(buf.toString());
        } catch (Exception e) {
            System.out.println("Unable to send keys back");
            return;
        }
        try {
            out.flush();
        } catch (Exception e) {
            System.out.println("Unable to send keys back");
        }
    }

    private void communicate() {
    }

    private static BigInteger calcPrivateSessionKey(BigInteger p) {
        Random rand = new Random();
        BigInteger randomLong = BigInteger.valueOf(rand.nextLong());
        BigInteger midState = p.multiply(randomLong);
        return midState.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE))[0];
    }

    private static BigInteger calcPublicKey(ClientData client, BigInteger privateKey) {
        return client.getG().modPow(privateKey, client.getP());
    }

    private static BigInteger calcSessionKey(BigInteger clientPublicKey, BigInteger privateKey, BigInteger clientP) {
        return clientPublicKey.modPow(privateKey, clientP);
    }

    private static BigInteger calcClientPublicKey(BigInteger xplust, ClientData client) {
        return client.getG().modPow(xplust, client.getP()).multiply(client.getTInv());
    }
}
