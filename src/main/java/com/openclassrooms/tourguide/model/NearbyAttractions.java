package com.openclassrooms.tourguide.model;

import gpsUtil.location.Location;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class NearbyAttractions {

    private Location userLocation;
    private final ArrayList<NearbyAttraction> attractions = new ArrayList<>();

    public NearbyAttractions(Location userLocation) {
        this.userLocation = userLocation;
    }
    public void add(NearbyAttraction nearbyAttraction) {
        attractions.add(nearbyAttraction);
    }
}
