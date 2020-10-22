/**
 * This program represent a client for Diffie-Hellman-Key-Exchange
 * To run the main class takes following command line inputs
 * ClientId -- a string representing client's Id
 */
package client;

import java.io.Console;
import java.math.BigInteger;
import java.util.Random;

public final class ClientDHKE {
    private static String id;
    private static final BigInteger p;
    private static final BigInteger g;
    private static BigInteger privateT;
    private static BigInteger publicT;
    private static Session session;

    private ClientDHKE() {
        // empty constructor
    }

    /**
     * Static Variables for the Client Class
     * p - The prime no
     * g - The generator for prime
     */
    static {
        p = new BigInteger("B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371", 16);
        g = new BigInteger("A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5", 16);
        calculateT();
    }

    /**
     * To wait for an confirmation from the command line
     * @param message  The format of message to print
     * @param args   Args represents the data to print
     */
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

    /**
     * A utility to calculate the permanent keys
     * and store their values in static variables
     */
    private static void calculateT() {
        try {
            privateT = calcPrivateT();
            publicT = g.modPow(privateT, p);
        } catch (Exception e) {
            System.out.println("Unable to generate new Permanent Keys");
        }
    }

    /**
     * Utility to calculate a private permanent Key
     * @return The calculated private key
     */
    private static BigInteger calcPrivateT() {
        Random rand = new Random();
        BigInteger randomLong = BigInteger.valueOf(rand.nextLong());
        BigInteger midState = p.multiply(randomLong);
        return midState.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE))[0];
    }

    /**
     * The starting point of calculations
     * @param args an array of command line inputs
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ClientId required");
            System.exit(1);
        }
        id = args[0];
        String message = "1. For new Connection type: connect <hostname> <port>\n2. newKeys to calculate new permanent Keys\n3. Anything else to exit application\n";
        newSession();
    }

    /**
     * A function to maintain a new client session
     * It creates a new session and does all key exchange part
     */
    private static void newSession() {
        waitForEnter("Send Connection Request To Server?");
        session = new Session("localhost", 9001);
        session.connect();
        waitForEnter("Send Key Exchange Request To Server? (Key Request Must be Sent within Two Minutes of Connection)");
        session.fullKeyRequest(id, p, privateT, publicT);
        waitForEnter("Receive Server's Public Key?");
        session.receiveKeys(p);
        session.close();
    }
}
