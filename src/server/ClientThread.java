package server;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;

public final class ClientThread extends Thread {
    private static final BigInteger P;
    private static final BigInteger G;
    private final String serverId;
    private final Socket socket;
    private BigInteger sessionKey;
    private long maxWait = 2 * 60 * 1000; // 2 minutes
    DataInputStream in = null;
    DataOutputStream out = null;

    static {
        P = new BigInteger("B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371", 16);
        G = new BigInteger("A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5", 16);
    }

    public static void waitForEnter(String message, Object... args) {
        Console c = System.console();
        if (c != null) {
            // printf-like arguments
            if (message != null)
                c.format(message, args);
            c.format(" --Press ENTER to proceed.");
            c.readLine();
        }
    }

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
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            if (!socket.isInputShutdown())
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
        /* Documentation */
        System.out.println("Connection Closed");
    }

    private void keyExchange() {
        if (in == null || out == null) {
            establishIO();
        }
        String[] clientInfo;
        // wait for KeyExchange to Initiate for 2 Minutes;
        long waitStart = System.currentTimeMillis();
        while(true) {
            try {
                clientInfo = in.readUTF().split("\\s+");
                break;
            } catch (IOException e) {
                if(System.currentTimeMillis() - waitStart > maxWait){
                    return;
                }
            }
        }
        String clientId = clientInfo[0];
        BigInteger xplust;
        if (clientInfo.length == 2) {
            BigInteger tInv = AuthManager.find(clientId);
            if (tInv == null) {
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
            try {
                t = new BigInteger(clientInfo[1]);
                xplust = new BigInteger(clientInfo[2]);
            } catch (Exception e) {
                System.out.println("Invalid keys provided");
                return;
            }
            AuthManager.addOrUpdate(clientId, t.modInverse(P));
        } else {
            System.out.println("Invalid Key Exchange");
            return;
        }
        /* Documentation */
        System.out.println("Key Exchange Request Received form: " + socket.getInetAddress());

        BigInteger privateKey = calcPrivateSessionKey();
        BigInteger publicKey = calcPublicKey(privateKey);

        /* Documentation */
        System.out.println("Sending Public Key back to Client: " + socket.getInetAddress());

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

        sessionKey = calcSessionKey(calcClientPublicKey(xplust, AuthManager.find(clientId)), privateKey);
        /* Documentation */
        System.out.println("Session Key Established");
        System.out.println("Session Key: " + sessionKey.toString());
    }

    private void communicate() {
    }

    private static BigInteger calcPrivateSessionKey() {
        Random rand = new Random();
        BigInteger randomLong = BigInteger.valueOf(rand.nextLong());
        BigInteger midState = P.multiply(randomLong);
        return midState.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE))[0];
    }

    private static BigInteger calcPublicKey(BigInteger privateKey) {
        return G.modPow(privateKey, P);
    }

    private static BigInteger calcSessionKey(BigInteger clientPublicKey, BigInteger privateKey) {
        return clientPublicKey.modPow(privateKey, P);
    }

    private static BigInteger calcClientPublicKey(BigInteger xplust, BigInteger tInv) {
        return G.modPow(xplust, P).multiply(tInv);
    }
}
