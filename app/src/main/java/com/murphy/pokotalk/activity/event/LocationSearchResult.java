package com.murphy.pokotalk.activity.event;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;

public class LocationSearchResult {
    private String title;
    private String link;
    private String category;
    private String description;
    private String address;
    private String roadAddress;
    private int mapX;
    private int mapY;
    private LatLng latLng;

    public LocationSearchResult() {
        title = null;
        link = null;
        category = null;
        description = null;
        address = null;
        roadAddress = null;
        latLng = null;
    }

    public String getTitle() {
        return title;
    }

    public LocationSearchResult setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public LocationSearchResult setLink(String link) {
        this.link = link;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public LocationSearchResult setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LocationSearchResult setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public LocationSearchResult setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public LocationSearchResult setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
        return this;
    }

    public int getMapX() {
        return mapX;
    }

    public LocationSearchResult setMapX(int mapX) {
        this.mapX = mapX;
        return this;
    }

    public int getMapY() {
        return mapY;
    }

    public LocationSearchResult setMapY(int mapY) {
        this.mapY = mapY;
        return this;
    }

    public LatLng getLatLng() {
        if (latLng != null) {
            return latLng;
        }

        // Convert Katech coordinate to LatLng coordinate
        return (new Tm128(mapX, mapY)).toLatLng();
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
