package com.unirc.tesi.marcoventura.contacttracing.cipher.blind_signature;

import java.math.BigInteger;

public class ResServlet {

    private BigInteger signature;

    public ResServlet() {
    }

    public ResServlet(BigInteger signature) {
        super();
        this.signature = signature;
    }

    public BigInteger getSignature() {
        return signature;
    }

    public void setSignature(BigInteger signature) {
        this.signature = signature;
    }

}
