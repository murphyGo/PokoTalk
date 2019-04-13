package com.murphy.pokotalk.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.murphy.pokotalk.data.db.schema.SessionSchema;

public class PokoSessionDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DB_NAME = "POKOTALK_SESSION_DB";
    protected static PokoSessionDatabase instance = null;

    public static PokoSessionDatabase getInstance(Context context) {
        if (instance == null && context != null) {
            // Create singleton database object
            synchronized (PokoSessionDatabase.class) {
                instance = instance == null ? new PokoSessionDatabase(context) : instance;
            }
        }

        return instance;
    }

    public PokoSessionDatabase(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create session table
        db.execSQL(SessionSchema.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We do not provide upgrade yet
    }
}
