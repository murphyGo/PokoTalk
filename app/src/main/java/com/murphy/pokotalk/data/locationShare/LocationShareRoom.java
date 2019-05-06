package com.murphy.pokotalk.data.locationShare;

import android.app.Activity;
import android.content.Context;

import com.murphy.pokotalk.data.Item;

import java.util.Calendar;

public class LocationShareRoom extends Item {
    private int eventId = -1;
    private LocationShareData locationData;
    private Calendar lastUpdated;
    private LocationShare myLocation;
    private MeetingLocation meetingLocation;
    private boolean joined = false;

    public LocationShareRoom() {
        locationData = new LocationShareData();
    }

    @Override
    public void update(Item item) {

    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocationShareData getLocationData() {
        return locationData;
    }

    public Calendar getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocationShare getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(LocationShare myLocation) {
        this.myLocation = myLocation;
    }

    public MeetingLocation getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(MeetingLocation meetingLocation) {
        this.meetingLocation = meetingLocation;
    }

    public boolean isJoined() {
        return joined;
    }

    public synchronized void setJoined(boolean joined) {
        this.joined = joined;
    }

    public void startMeasure(Context context, Activity activity) {
        // Start measuring location
        LocationMeasure.startMeasure(context, activity);

        // Attach room
        LocationMeasure.attachRoom(this);
    }

    public void stopMeasure() {
        LocationMeasure.detachRoom(this);
    }
}
