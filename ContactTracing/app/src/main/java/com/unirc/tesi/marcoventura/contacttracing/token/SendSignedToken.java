package com.unirc.tesi.marcoventura.contacttracing.token;

import java.util.ArrayList;

public class SendSignedToken {

    private String signature;
    private ArrayList<Token> token_array;

    public SendSignedToken() {
    }

    public SendSignedToken(String signature, ArrayList<Token> token_array) {
        this.signature = signature;
        this.token_array = token_array;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public ArrayList<Token> getToken_array() {
        return token_array;
    }

    public void setToken_array(ArrayList<Token> token_array) {
        this.token_array = token_array;
    }
}
