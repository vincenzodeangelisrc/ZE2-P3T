package com.unirc.tesi.marcoventura.contacttracing.token;

public class Token {

    private int id;
    private String random_token;
    private String mac_address;

    public Token(){
    }

    public Token(int id, String random_token, String mac_address) {
        this.id = id;
        this.random_token = random_token;
        this.mac_address = mac_address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRandom_token() {
        return random_token;
    }

    public void setRandom_token(String random_token) {
        this.random_token = random_token;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }
}
