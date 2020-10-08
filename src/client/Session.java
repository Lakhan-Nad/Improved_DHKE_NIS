package client;

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

    public void connect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("Client Busy");
            }
        }
        try {
            socket = new Socket(ip, port);
        } catch (Exception e) {
            System.out.println("Invalid Host or Port provided");
        }
        establishIO();
    }

    private void establishIO() {
        if (socket == null) {
            System.out.println("Establish a Connection Before");
            return;
        }
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println("Input Channel Error");
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println("Output Channel Error");
            }
        }
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Unable to open a I/O Channel");
        }
    }

    private void receiveKeys(BigInteger p) {
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
        System.out.println(sessionKey.toString());
    }

    public void fullKeyExchange(String id, BigInteger p, BigInteger g, BigInteger privateT, BigInteger publicT) {
        if (socket == null) {
            System.out.println("First Establish a Connection");
            return;
        }
        if (in == null || out == null) {
            establishIO();
        }
        privateSessionKey = calcPrivateSessionKey(p);
        BigInteger publicSessionKey = calcPublicSessionKey(privateT, p);
        // sending keys
        StringBuilder buf = new StringBuilder();
        buf.append(id);
        buf.append(' ');
        buf.append(g.toString());
        buf.append(' ');
        buf.append(p.toString());
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
        // receiving keys
        receiveKeys(p);
    }

    public void keyExchangeWithNewT(String id, BigInteger p, BigInteger privateT, BigInteger publicT) {
        if (socket == null) {
            System.out.println("First Establish a Connection");
            return;
        }
        if (in == null || out == null) {
            establishIO();
        }
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
        // receiving keys
        receiveKeys(p);
    }

    public void keyExchange(String id, BigInteger p, BigInteger privateT) {
        if (socket == null) {
            System.out.println("First Establish a Connection");
            return;
        }
        if (in == null || out == null) {
            establishIO();
        }
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
        // receiving keys
        receiveKeys(p);
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
    }

    public void communicate() {
    }
}
