package com.unirc.tesi.marcoventura.contacttracing.ble;

import java.util.ArrayList;

public class SendClientData {

    private String user_random, hash;
    private long timestamp;
    private ArrayList<BleContactServer> list;
    private long time_of_contact;

    public SendClientData(String user_random, String hash, long timestamp,
                          ArrayList<BleContactServer> list, long time_of_contact) {
        this.user_random = user_random;
        this.hash = hash;
        this.timestamp = timestamp;
        this.list = list;
        this.time_of_contact = time_of_contact;
    }

    public String getUser_random() {
        return user_random;
    }

    public void setUser_random(String user_random) {
        this.user_random = user_random;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<BleContactServer> getList() {
        return list;
    }

    public void setList(ArrayList<BleContactServer> list) {
        this.list = list;
    }

    public long getTime_of_contact() {
        return time_of_contact;
    }

    public void setTime_of_contact(long time_of_contact) {
        this.time_of_contact = time_of_contact;
    }
}
