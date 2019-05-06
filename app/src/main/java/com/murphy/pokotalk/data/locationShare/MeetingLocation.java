package com.murphy.pokotalk.data.locationShare;

import com.naver.maps.geometry.LatLng;

public class MeetingLocation {
    private String locationName;
    private String description;
    private LatLng latLng;

    public MeetingLocation(String locationName, String description, LatLng latLng) {
        this.locationName = locationName;
        this.description = description;
        this.latLng = latLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
