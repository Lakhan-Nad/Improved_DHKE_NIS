package server;

import java.math.BigInteger;

class ClientData {
    public String id;
    private BigInteger p;
    private BigInteger g;
    private BigInteger tInv;

    ClientData() {
        p = null;
        g = null;
        tInv = null;
    }
    ClientData(String id, BigInteger g, BigInteger p, BigInteger tInv){
        this.id = id;
        this.g = g;
        this.p = p;
        this.tInv = tInv;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getTInv() {
        return tInv;
    }

    public void setTInv(BigInteger tInv) {
        this.tInv = tInv;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public void setG(BigInteger g) {
        this.g = g;
    }
}
