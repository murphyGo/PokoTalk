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

    private LocationShareRoom getRoomFromRooms(int eventId) {
        // Get rooms
        LocationShareRooms rooms = LocationShareRooms.getInstance();

        // Initialize location data
        LocationShareRoom room = rooms.getItemByKey(eventId);

        return room;
    }

    public synchronized LocationShareRoom getRoom(int eventId) {
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
        LocationShareRoom room = getRoomFromRooms(eventId);

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
        LocationShareRoom room = getRoomFromRooms(eventId);

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

                LocationShare share = new LocationShare();
                share.setUser(user);
                share.setNumber(myNumber);

                // Get my location
                LocationShare myLocation = data.getItemByKey(data.getKey(share));

                if (myLocation == null) {
                    // Put my location
                    data.updateItem(share);

                    // Set my location
                    room.setMyLocation(share);
                } else {
                    // Set my location
                    room.setMyLocation(myLocation);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void removeRoom(int eventId) {
        // Get room
        LocationShareRoom room = getRoomFromRooms(eventId);

        if (room != null) {
            // Set exited
            room.setJoined(false);

            // Stop measuring
            room.stopMeasure();

            // Get rooms
            LocationShareRooms rooms = LocationShareRooms.getInstance();

            // Remove room from room list
            rooms.removeItemByKey(rooms.getKey(room));
        }
    }
}
