package com.unirc.tesi.marcoventura.contacttracing.ble;

public class BleHandshake {

    private String mac_address;
    private long timestamp;
    private int random;
    private int centroid;
    private double distance;
    private String hash;
    private boolean calculate_parameter;
    private boolean continue_gps;

    public BleHandshake() {
    }

    public BleHandshake(String mac_address, long timestamp, int random, int centroid, double distance, String hash, boolean calculate_parameter, boolean continue_gps) {
        this.mac_address = mac_address;
        this.timestamp = timestamp;
        this.random = random;
        this.centroid = centroid;
        this.distance = distance;
        this.hash = hash;
        this.calculate_parameter = calculate_parameter;
        this.continue_gps = continue_gps;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getRandom() {
        return random;
    }

    public void setRandom(int random) {
        this.random = random;
    }

    public int getCentroid() {
        return centroid;
    }

    public void setCentroid(int centroid) {
        this.centroid = centroid;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isCalculate_parameter() {
        return calculate_parameter;
    }

    public void setCalculate_parameter(boolean calculate_parameter) {
        this.calculate_parameter = calculate_parameter;
    }

    public boolean isContinue_gps() {
        return continue_gps;
    }

    public void setContinue_gps(boolean continue_gps) {
        this.continue_gps = continue_gps;
    }
}
