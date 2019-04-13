package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class MessagesSchema {
    private MessagesSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.GROUP_ID + " INTEGER NOT NULL, " +
                    Entry.MESSAGE_ID + " INTEGER NOT NULL, " +
                    Entry.MESSAGE_TYPE + " INTEGER NOT NULL, " +
                    Entry.USER_ID + " INTEGER NOT NULL, " +
                    Entry.DATE + " INTEGER NOT NULL, " +
                    Entry.IMPORTANCE + " INTEGER, " +
                    Entry.NB_NOT_READ + " INTEGER, " +
                    Entry.CONTENTS + " TEXT, " +
                    Entry.SPECIAL_CONTENTS + " TEXT, " +
                    "PRIMARY KEY (" + Entry.GROUP_ID + ", " + Entry.MESSAGE_ID + "), " +
                    "FOREIGN KEY (" + Entry.GROUP_ID + ") REFERENCES " +
                    GroupsSchema.Entry.TABLE_NAME + "(" + GroupsSchema.Entry.GROUP_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + Entry.USER_ID + ") REFERENCES " +
                    UsersSchema.Entry.TABLE_NAME + "(" + UsersSchema.Entry.USER_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Messages";
        public static final String GROUP_ID = "groupId";
        public static final String MESSAGE_ID = "messageId";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String USER_ID = "userId";
        public static final String IMPORTANCE = "importance";
        public static final String NB_NOT_READ = "nbNotRead";
        public static final String CONTENTS = "contents";
        public static final String SPECIAL_CONTENTS = "specialContents";
        public static final String DATE = "date";
    }
}
