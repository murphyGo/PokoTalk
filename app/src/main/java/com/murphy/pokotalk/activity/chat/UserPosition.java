package com.murphy.pokotalk.activity.chat;

import com.naver.maps.map.overlay.Marker;

public class UserPosition {
    private String key;
    private boolean updated = false;
    private Marker marker;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}