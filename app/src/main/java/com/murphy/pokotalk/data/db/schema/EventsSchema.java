package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class EventsSchema {
    private EventsSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.EVENT_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    Entry.EVENT_CREATOR + " INTEGER NOT NULL, " +
                    Entry.EVENT_NAME + " TEXT NOT NULL, " +
                    Entry.EVENT_DESCRIPTION + " TEXT, " +
                    Entry.EVENT_STARTED + " INTEGER NOT NULL, " +
                    Entry.EVENT_DATE + " INTEGER NOT NULL, " +
                    Entry.GROUP_ID + " INTEGER, " +
                    Entry.ACK + " INTEGER NOT NULL)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Events";
        public static final String TABLE_NAME_TEMP = "Events_temp";
        public static final String EVENT_ID = "eventId";
        public static final String EVENT_CREATOR = "eventCreator";
        public static final String EVENT_NAME = "eventName";
        public static final String EVENT_DESCRIPTION = "eventDescription";
        public static final String EVENT_STARTED = "eventStarted";
        public static final String EVENT_DATE = "eventDate";
        public static final String GROUP_ID = "groupId";
        public static final String ACK = "ack";
    }
}
