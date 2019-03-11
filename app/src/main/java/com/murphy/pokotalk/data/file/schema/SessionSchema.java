package com.murphy.pokotalk.data.file.schema;

import android.provider.BaseColumns;

public class SessionSchema {
    private SessionSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry._ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    Entry.SESSION_ID + " TEXT NOT NULL, " +
                    Entry.USER_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + Entry.USER_ID + ") REFERENCES " +
                    UsersSchema.Entry.TABLE_NAME + "(" + UsersSchema.Entry.USER_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";


    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Session";
        public static final String SESSION_ID = "sessionId";
        public static final String USER_ID = "userId";
    }
}