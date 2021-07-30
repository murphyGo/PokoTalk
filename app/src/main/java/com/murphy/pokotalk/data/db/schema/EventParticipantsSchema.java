package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class EventParticipantsSchema {
    private EventParticipantsSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.EVENT_ID + " INTEGER NOT NULL, " +
                    Entry.PARTICIPANT_ID + " INTEGER NOT NULL, " +
                    "PRIMARY KEY (" + Entry.EVENT_ID + ", " + Entry.PARTICIPANT_ID + "), " +
                    "FOREIGN KEY (" + Entry.EVENT_ID + ") REFERENCES " +
                    EventsSchema.Entry.TABLE_NAME + "(" + EventsSchema.Entry.EVENT_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + Entry.PARTICIPANT_ID + ") REFERENCES " +
                    UsersSchema.Entry.TABLE_NAME + "(" + UsersSchema.Entry.USER_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "EventParticipants";
        public static final String EVENT_ID = "eventId";
        public static final String PARTICIPANT_ID = "participantId";
    }
}
