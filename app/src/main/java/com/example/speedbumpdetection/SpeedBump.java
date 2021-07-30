package com.example.speedbumpdetection;

public class SpeedBump {

    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }



    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public SpeedBump(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public SpeedBump(){

    };
}
