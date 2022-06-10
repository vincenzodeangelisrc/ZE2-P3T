package com.unirc.tesi.marcoventura.contacttracing.ble;

public class BleKeepAlive {

    private String address;
    private boolean received;

    public BleKeepAlive(String address, boolean received) {
        this.address = address;
        this.received = received;
    }

    public BleKeepAlive() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
