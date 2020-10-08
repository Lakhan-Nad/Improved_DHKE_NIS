package client;

import java.math.BigInteger;
import java.util.Random;

public final class ClientDHKE {
    private static String id;
    private static final BigInteger p;
    private static final BigInteger g;
    private static BigInteger privateT;
    private static BigInteger publicT;

    private ClientDHKE() {
        // empty constructor
    }

    static {
        p = new BigInteger("B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371", 16);
        g = new BigInteger("A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5", 16);
        privateT = calcPrivateT();
        publicT = g.modPow(privateT, p);
    }

    private static void calculateT() {
        try {
            privateT = calcPrivateT();
            publicT = g.modPow(privateT, p);
        } catch (Exception e) {
            System.out.println("Unable to generate new Permanent Keys");
        }
    }

    private static BigInteger calcPrivateT() {
        Random rand = new Random();
        BigInteger randomLong = BigInteger.valueOf(rand.nextLong());
        BigInteger midState = p.multiply(randomLong);
        return midState.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE))[0];
    }

    public static void main(String[] args) {
        id = "Client1";
        Session session = new Session("localhost", 9001);
        session.connect();
        session.fullKeyExchange(id, p, g, privateT, publicT);
        session.close();
    }
}
