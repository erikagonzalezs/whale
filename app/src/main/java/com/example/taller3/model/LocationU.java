package com.example.taller3.model;

public class LocationU {
    public String name;
    public double lat;
    public double longit;

    public LocationU(String name, double lat, double longit) {
        this.name = name;
        this.lat = lat;
        this.longit = longit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongit() {
        return longit;
    }

    public void setLongit(double longit) {
        this.longit = longit;
    }
}
