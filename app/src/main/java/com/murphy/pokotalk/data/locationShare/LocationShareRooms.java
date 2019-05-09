package com.murphy.pokotalk.data.locationShare;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class LocationShareRooms extends ItemPokoList<Integer, LocationShareRoom> {
    private static LocationShareRooms instance = null;

    public static LocationShareRooms getInstance() {
        if (instance == null) {
            synchronized (LocationShareRooms.class) {
                instance = instance == null ? new LocationShareRooms() : instance;
            }
        }

        return instance;
    }

    @Override
    public Integer getKey(LocationShareRoom locationShareRoom) {
        return locationShareRoom.getEventId();
    }

    public static synchronized void clear() {
        if (instance != null) {
            // Remove instance
            instance = null;
        }
    }
}
