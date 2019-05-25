package com.murphy.pokotalk.data.locationShare;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.murphy.pokotalk.data.Item;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LocationShareRoom extends Item {
    private int eventId = -1;
    private LocationShareData locationData;
    private Calendar lastUpdated;
    private LocationShare myLocation;
    private MeetingLocation meetingLocation;
    private boolean joined = false;
    private List<RoomStateChangeListener> stateChangeListeners = null;
    private Handler handler;

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

    public synchronized void setJoined(final boolean joined) {
        this.joined = joined;

        if (stateChangeListeners != null) {
            for (final RoomStateChangeListener listener : stateChangeListeners) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChange(joined);
                    }
                });
            }
        }
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

    public static abstract class RoomStateChangeListener {
        public abstract void initialState(boolean joined);
        public abstract void onStateChange(boolean joined);
    }

    public synchronized void addListener(final RoomStateChangeListener listener) {
        if (stateChangeListeners == null) {
            stateChangeListeners = new ArrayList<>();
            handler = new Handler(Looper.getMainLooper());
        }

        // Add to listeners
        stateChangeListeners.add(listener);

        // Start initial callback
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.initialState(joined);
            }
        });
    }

    public synchronized void removeListener(RoomStateChangeListener listener) {
        if (stateChangeListeners == null) {
            return;
        }

        // Remove listener
        stateChangeListeners.remove(listener);

        if (stateChangeListeners.size() == 0) {
            // Removes list if empty
            stateChangeListeners = null;
            handler = null;
        }
    }
}
