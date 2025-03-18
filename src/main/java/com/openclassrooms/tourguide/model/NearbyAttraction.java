package com.openclassrooms.tourguide.model;

import gpsUtil.location.Attraction;

public class NearbyAttraction {
    private String attractionName="";
    private double longitude=0.0, latitude=0.0;
    private double distance=0.0;
    private int rewardPoint=0;

    public NearbyAttraction(Attraction attraction, double distance, int rewardPoint) {
        this.attractionName = attraction.attractionName;
        this.latitude = attraction.latitude;
        this.longitude = attraction.longitude;
        this.distance = distance;
        this.rewardPoint = rewardPoint;
    }
}
