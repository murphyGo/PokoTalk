package com.murphy.pokotalk.data.file.schema;

import android.provider.BaseColumns;

public class GroupMembersSchema {
    private GroupMembersSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.GROUP_ID + " INTEGER NOT NULL, " +
                    Entry.USER_ID + " INTEGER NOT NULL, " +
                    Entry.PENDING + " INTEGER NOT NULL, " +
                    Entry.INVITED + " INTEGER, " +
                    "PRIMARY KEY (" + Entry.GROUP_ID + ", " + Entry.USER_ID + "), " +
                    "FOREIGN KEY (" + Entry.GROUP_ID + ") REFERENCES " +
                    GroupsSchema.Entry.TABLE_NAME + "(" + GroupsSchema.Entry.GROUP_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + Entry.USER_ID + ") REFERENCES " +
                    UsersSchema.Entry.TABLE_NAME + "(" + UsersSchema.Entry.USER_ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "GroupMembers";
        public static final String GROUP_ID = "groupId";
        public static final String USER_ID = "userId";
        public static final String PENDING = "pending";
        public static final String INVITED = "invited";
    }
}
