package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.Item;
import com.naver.maps.geometry.LatLng;

import java.util.Calendar;

public class EventLocation extends Item {
    protected String title;
    protected String category;
    protected String address;
    protected LatLng latLng;
    protected Calendar meetingDate;

    @Override
    public void update(Item item) {
        EventLocation location = (EventLocation) item;

        setTitle(location.getTitle());
        setCategory(location.getCategory());
        setAddress(location.getAddress());
        setLatLng(location.getLatLng());
        setMeetingDate(location.getMeetingDate());
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

    public Calendar getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Calendar meetingDate) {
        this.meetingDate = meetingDate;
    }
}
