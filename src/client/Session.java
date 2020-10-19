package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;

class Session {
    private String serverId;
    private BigInteger sessionKey;
    private BigInteger privateSessionKey;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final String ip;
    private final int port;

    public Session(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private static BigInteger calcPrivateSessionKey(BigInteger p) {
        Random rand = new Random();
        BigInteger randomLong = BigInteger.valueOf(rand.nextLong());
        BigInteger midState = p.multiply(randomLong);
        return midState.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE))[0];
    }

    private BigInteger calcPublicSessionKey(BigInteger privateT, BigInteger p) {
        return privateSessionKey.add(privateT).mod(p.subtract(BigInteger.ONE));
    }

    private BigInteger calcSessionKey(BigInteger serverPublicKey, BigInteger p) {
        return serverPublicKey.modPow(privateSessionKey, p);
    }

    public boolean connect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("Client busy couldn't establish new connection");
            }
        }
        /* Documentation */
        System.out.println("Sending Connection Request To Server");
        try {
            socket = new Socket(ip, port);
        } catch (Exception e) {
            System.out.println("Invalid Host or Port provided");
            return false;
        }
        return establishIO();
    }

    private boolean establishIO() {
        if (socket == null) {
            System.out.println("Establish a Connection Before");
            return false;
        }
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println("Input Channel Error");
                return false;
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println("Output Channel Error");
                return false;
            }
        }
        try {
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Unable to open a I/O Channel");
            return false;
        }
        return true;
    }

    public void receiveKeys(BigInteger p) {
        System.out.println("Waiting for Server's Public Key");
        String[] receivedData;
        try {
            receivedData = in.readUTF().split("\\s+");
        } catch (Exception e) {
            System.out.println("Unable to get back Server's Public Key");
            return;
        }
        serverId = receivedData[0];
        BigInteger serverPublicKey;
        try {
            serverPublicKey = new BigInteger(receivedData[1]);
        } catch (Exception e) {
            System.out.println("Invalid key received");
            return;
        }
        sessionKey = calcSessionKey(serverPublicKey, p);
        /* Documentation */
        System.out.println("Session key Established");
        System.out.println("Session Key:" + sessionKey.toString());
    }

    public void fullKeyRequest(String id, BigInteger p, BigInteger privateT, BigInteger publicT) {
        if (socket == null) {
            System.out.println("First Establish a Connection");
            return;
        }
        if (in == null || out == null) {
            establishIO();
        }
        /* Documentation */
        System.out.println("Starting Key Exchange");
        privateSessionKey = calcPrivateSessionKey(p);
        BigInteger publicSessionKey = calcPublicSessionKey(privateT, p);
        // sending keys
        StringBuilder buf = new StringBuilder();
        buf.append(id);
        buf.append(' ');
        buf.append(publicT.toString());
        buf.append(' ');
        buf.append(publicSessionKey.toString());
        buf.append('\n');
        try {
            out.writeUTF(buf.toString());
            out.flush();
        } catch (Exception e) {
            System.out.println("Unable to initiate a session");
        }
        /* Documentation */
        System.out.println("Request for Key Exchange Sent to Server");
    }

    public void keyRequest(String id, BigInteger p, BigInteger privateT) {
        if (socket == null) {
            System.out.println("First Establish a Connection");
            return;
        }
        if (in == null || out == null) {
            establishIO();
        }
        /* Documentation */
        System.out.println("Starting Key Exchange");
        privateSessionKey = calcPrivateSessionKey(p);
        BigInteger publicSessionKey = calcPublicSessionKey(privateT, p);
        // sending keys
        StringBuilder buf = new StringBuilder();
        buf.append(id);
        buf.append(' ');
        buf.append(publicSessionKey.toString());
        buf.append('\n');
        try {
            out.writeUTF(buf.toString());
            out.flush();
        } catch (Exception e) {
            System.out.println("Unable to initiate a session");
        }
        /* Documentation */
        System.out.println("Request for Key Exchange Sent to Server");
    }

    public void close() {
        try {
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        } catch (Exception e) {
            System.out.println("Unable to close Resources");
        }
        /* Documentation */
        System.out.println("Connection with Server Closed");
    }

    public void communicate() {
    }
}
