package com.unirc.tesi.marcoventura.contacttracing.ble;

public class BleConnection {

    private String address;
    private long start_timestamp;

    public BleConnection() {
    }

    public BleConnection(String address, long start_timestamp) {
        this.address = address;
        this.start_timestamp = start_timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getStart_timestamp() {
        return start_timestamp;
    }

    public void setStart_timestamp(long start_timestamp) {
        this.start_timestamp = start_timestamp;
    }
}
