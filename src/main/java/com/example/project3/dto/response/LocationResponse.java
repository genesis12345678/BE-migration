package com.example.project3.dto.response;

public class LocationResponse {
    private int locationId;
    private double latitude;
    private double longitude;

    public LocationResponse() {

    }

    public LocationResponse(int locationId, double latitude, double longitude) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LocationResponse{" +
                "locationId=" + locationId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
