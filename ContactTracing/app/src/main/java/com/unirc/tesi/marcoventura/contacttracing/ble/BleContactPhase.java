package com.unirc.tesi.marcoventura.contacttracing.ble;

public class BleContactPhase {

    private String mac_address;
    private double distance;
    private double angle;
    private long timestamp;

    public BleContactPhase(String mac_address, double distance, double angle, long timestamp) {
        this.mac_address = mac_address;
        this.distance = distance;
        this.angle = angle;
        this.timestamp = timestamp;
    }

    public BleContactPhase() {
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
