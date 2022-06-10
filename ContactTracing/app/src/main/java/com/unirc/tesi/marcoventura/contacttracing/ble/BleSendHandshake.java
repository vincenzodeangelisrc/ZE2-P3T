package com.unirc.tesi.marcoventura.contacttracing.ble;


public class BleSendHandshake {

    private String address_to_tx;
    private long timestamp_tx;
    private int random_tx;
    private int centroid_tx;
    private double distance_tx;

    public BleSendHandshake() {
    }

    public BleSendHandshake(String address_to_tx, long timestamp_tx, int random_tx, int centroid_tx, double distance_tx) {
        this.address_to_tx = address_to_tx;
        this.timestamp_tx = timestamp_tx;
        this.random_tx = random_tx;
        this.centroid_tx = centroid_tx;
        this.distance_tx = distance_tx;
    }

    public String getAddress_to_tx() {
        return address_to_tx;
    }

    public void setAddress_to_tx(String address_to_tx) {
        this.address_to_tx = address_to_tx;
    }

    public long getTimestamp_tx() {
        return timestamp_tx;
    }

    public void setTimestamp_tx(long timestamp_tx) {
        this.timestamp_tx = timestamp_tx;
    }

    public int getRandom_tx() {
        return random_tx;
    }

    public void setRandom_tx(int random_tx) {
        this.random_tx = random_tx;
    }

    public int getCentroid_tx() {
        return centroid_tx;
    }

    public void setCentroid_tx(int centroid_tx) {
        this.centroid_tx = centroid_tx;
    }

    public double getDistance_tx() {
        return distance_tx;
    }

    public void setDistance_tx(double distance_tx) {
        this.distance_tx = distance_tx;
    }
}
