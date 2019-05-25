package com.murphy.pokotalk.data.locationShare;

import android.util.Log;

import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/** Synchronized helper class for location share */
public class LocationShareHelper {
    private static LocationShareHelper instance = null;

    public static LocationShareHelper getInstance() {
        if (instance == null) {
            synchronized (LocationShareHelper.class) {
                instance = instance == null ? new LocationShareHelper() : instance;
            }
        }

        return instance;
    }

    public synchronized LocationShareRoom getRoom(int eventId) {
        // Get rooms
        LocationShareRooms rooms = LocationShareRooms.getInstance();

        // Initialize location data
        LocationShareRoom room = rooms.getItemByKey(eventId);

        return room;
    }

    public synchronized LocationShareRoom getRoomOrCreateIfNotExists(int eventId) {
        // Get rooms
        LocationShareRooms rooms = LocationShareRooms.getInstance();

        // Get room from rooms
        LocationShareRoom room = rooms.getItemByKey(eventId);

        if (room == null) {
            // Create room
            room = new LocationShareRoom();
            room.setEventId(eventId);

            // Add room
            rooms.updateItem(room);
        }

        return room;
    }

    public synchronized void updateMyLocation(LocationShareRoom room, LatLng latLng) {
        if (room != null) {
            // Get my location
            LocationShare locationShare = room.getMyLocation();

            if (locationShare != null) {
                // Update latitude and longitude
                locationShare.setLatLng(latLng);
            }
        }
    }

    public synchronized void updateLocations(int eventId, JSONArray locations, Calendar date) {
        // Get room
        LocationShareRoom room = getRoom(eventId);

        if (room != null) {
            LocationShareData locationShareData = room.getLocationData();

            // Update calendar
            room.setLastUpdated(date);

            // Start update
            locationShareData.startUpdateList();

            for (int i = 0; i < locations.length(); i++) {
                try {
                    // Get json object
                    JSONObject jsonObject = locations.getJSONObject(i);

                    // Parse location
                    LocationShare share = PokoParser.parseLocationShare(jsonObject);

                    // Update location
                    locationShareData.updateItem(share);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Finish update
            locationShareData.endUpdateList();
        }
    }

    public synchronized void setRoomJoined(int eventId, int myNumber,
                                                             JSONObject jsonObject) {
        // Get room
        LocationShareRoom room = getRoom(eventId);

        // Get me
        Contact user = Session.getInstance().getUser();

        if (room != null && user != null) {
            try {
                Log.v("POKO", "LOCATION SHARE JOINED");
                // Set room joined
                room.setJoined(true);

                // Parse meeting location
                MeetingLocation location = PokoParser.parseLocationShareMeetingLocation(jsonObject);

                // Set meeting location
                room.setMeetingLocation(location);

                // Get location data
                LocationShareData data = room.getLocationData();

                // Make new my location
                LocationShare newMyLocation = new LocationShare();

                // Set data
                newMyLocation.setUser(user);
                newMyLocation.setNumber(myNumber);

                // My location should survive on list update
                newMyLocation.setSurviveOnListUpdate(true);

                // Get my location
                LocationShare myLocation = room.getMyLocation();

                if (myLocation != null) {
                    // Remove old my location
                    data.removeItemByKey(data.getKey(myLocation));
                }

                // Put my location
                data.updateItem(newMyLocation);

                // Set my location
                room.setMyLocation(newMyLocation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopRoom(int eventId) {
        // Get room
        LocationShareRoom room = getRoom(eventId);

        if (room != null) {
            // Set exited
            room.setJoined(false);

            // Stop measuring
            room.stopMeasure();
        }
    }
}
