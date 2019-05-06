package com.murphy.pokotalk.data.locationShare;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.user.User;
import com.naver.maps.geometry.LatLng;

import java.util.Calendar;

public class LocationShare extends Item {
    private User user;
    private int number = -1;
    private LatLng latLng;
    private Calendar calendar;

    @Override
    public void update(Item item) {
        LocationShare share = (LocationShare) item;

        setLatLng(share.getLatLng());
        setCalendar(share.getCalendar());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
