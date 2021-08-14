package com.example.speedbumpdetection;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {
    private Polyline polyline;
    private DirectionsLeg directionsLeg;

    public Polyline getPolyline() {
        return polyline;
    }

    public DirectionsLeg getDirectionsLeg() {
        return directionsLeg;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public void setDirectionsLeg(DirectionsLeg directionsLeg) {
        this.directionsLeg = directionsLeg;
    }

    public PolylineData(Polyline polyline, DirectionsLeg directionsLeg){
        this.polyline = polyline;
        this.directionsLeg = directionsLeg;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", directionsLeg=" + directionsLeg +
                '}';
    }
}
