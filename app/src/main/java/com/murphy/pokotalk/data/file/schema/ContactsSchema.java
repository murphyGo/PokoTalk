package com.murphy.pokotalk.data.file.schema;

import android.provider.BaseColumns;

public class ContactsSchema {
    private ContactsSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.USER_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    Entry.GROUP_CHAT_ID + " INTEGER, " +
                    Entry.PENDING + " INTEGER NOT NULL, " +
                    Entry.INVITED + " INTEGER, " +
                    "FOREIGN KEY (" + Entry.USER_ID + ") REFERENCES " +
                    UsersSchema.Entry.TABLE_NAME + " (" + UsersSchema.Entry.USER_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + Entry.GROUP_CHAT_ID + ") REFERENCES " +
                    GroupsSchema.Entry.TABLE_NAME + " (" + GroupsSchema.Entry.GROUP_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Contacts";
        public static final String USER_ID = "userId";
        public static final String GROUP_CHAT_ID = "groupChatId";
        public static final String PENDING = "pending";
        public static final String INVITED = "invited";
    }
}
