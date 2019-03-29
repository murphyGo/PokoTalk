package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.Item;
import com.naver.maps.geometry.LatLng;

public class EventLocation extends Item {
    protected String title;
    protected String category;
    protected String address;
    protected LatLng latLng;

    @Override
    public void update(Item item) {
        EventLocation location = (EventLocation) item;

        this.title = location.getTitle();
        this.category = location.getCategory();
        this.address = location.getAddress();
        this.latLng = location.getLatLng();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
