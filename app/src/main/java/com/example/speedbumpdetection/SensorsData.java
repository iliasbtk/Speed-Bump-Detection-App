package com.example.speedbumpdetection;

public class SensorsData {
    private float accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z;
    double lat, lon, alt;
    private int id;
    String date;

    public SensorsData() {
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setAccel_x(float accel_x) {
        this.accel_x = accel_x;
    }

    public void setAccel_y(float accel_y) {
        this.accel_y = accel_y;
    }

    public void setAccel_z(float accel_z) {
        this.accel_z = accel_z;
    }

    public void setGyro_x(float gyro_x) {
        this.gyro_x = gyro_x;
    }

    public void setGyro_y(float gyro_y) {
        this.gyro_y = gyro_y;
    }

    public void setGyro_z(float gyro_z) {
        this.gyro_z = gyro_z;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate(){
        return date;
    }

    public float getAccel_x() {
        return accel_x;
    }

    public float getAccel_y() {
        return accel_y;
    }

    public float getAccel_z() {
        return accel_z;
    }

    public float getGyro_x() {
        return gyro_x;
    }

    public float getGyro_y() {
        return gyro_y;
    }

    public float getGyro_z() {
        return gyro_z;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    public int getId() {
        return id;
    }

}
