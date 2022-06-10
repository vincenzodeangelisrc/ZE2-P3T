package com.unirc.tesi.marcoventura.contacttracing.ble;

public class BleContactServer {

    private double distance, angle;
    private long timestamp;

    public BleContactServer() {
    }

    public BleContactServer(double distance, double angle, long timestamp) {
        this.distance = distance;
        this.angle = angle;
        this.timestamp = timestamp;
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
