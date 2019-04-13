package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class EventLocationSchema {
    private EventLocationSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.EVENT_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    Entry.LOCATION_TITLE + " INTEGER NOT NULL, " +
                    Entry.LOCATION_CATEGORY + " TEXT, " +
                    Entry.LOCATION_ADDRESS + " TEXT, " +
                    Entry.LOCATION_MEETING_DATE + " INTEGER NOT NULL, " +
                    Entry.LOCATION_LATITUDE + " REAL NOT NULL, " +
                    Entry.LOCATION_LONGITUDE + " REAL NOT NULL, " +
                    "FOREIGN KEY (" + Entry.EVENT_ID + ") REFERENCES " +
                    EventsSchema.Entry.TABLE_NAME + "(" + EventsSchema.Entry.EVENT_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "EventLocations";
        public static final String EVENT_ID = "eventId";
        public static final String LOCATION_TITLE = "locationDate";
        public static final String LOCATION_CATEGORY = "locationCategory";
        public static final String LOCATION_ADDRESS = "locationAddress";
        public static final String LOCATION_MEETING_DATE = "locationMeetingDate";
        public static final String LOCATION_LATITUDE = "locationLatitude";
        public static final String LOCATION_LONGITUDE = "locationLongitude";
    }
}
