package com.murphy.pokotalk.data.db.schema;

import android.provider.BaseColumns;

public class GroupsSchema {
    private GroupsSchema() {

    }

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Entry.TABLE_NAME + " (" +
                    Entry.GROUP_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    Entry.NAME + " TEXT, " +
                    Entry.ALIAS + " TEXT, " +
                    Entry.NB_NEW_MESSAGES + " INTEGER NOT NULL, " +
                    Entry.ACK + " INTEGER NOT NULL)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE " + Entry.TABLE_NAME;


    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "Groups";
        public static final String GROUP_ID = "groupId";
        public static final String NAME = "name";
        public static final String ALIAS = "alias";
        public static final String NB_NEW_MESSAGES = "nbNewMessages";
        public static final String ACK = "ack";
    }
}
