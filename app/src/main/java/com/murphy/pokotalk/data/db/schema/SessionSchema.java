package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class SessionSchema {
    private SessionSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.SESSION_ID + " TEXT NOT NULL PRIMARY KEY, " +
                    Entry.SESSION_EXPIRE + " INTEGER NOT NULL, " +
                    Entry.USER_ID + " INTEGER NOT NULL, " +
                    Entry.EMAIL + " TEXT NOT NULL, " +
                    Entry.NICKNAME + " TEXT NOT NULL, " +
                    Entry.PICTURE + " TEXT, " +
                    Entry.LAST_SEEN + " INTEGER)";


    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Session";
        public static final String SESSION_ID = "sessionId";
        public static final String SESSION_EXPIRE = "sessionExpire";
        public static final String USER_ID = "userId";
        public static final String EMAIL = "email";
        public static final String NICKNAME = "nickname";
        public static final String PICTURE = "picture";
        public static final String LAST_SEEN = "lastSeen";
    }
}