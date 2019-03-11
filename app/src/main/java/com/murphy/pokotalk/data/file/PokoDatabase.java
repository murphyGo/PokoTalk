package com.murphy.pokotalk.data.file;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.murphy.pokotalk.data.file.schema.ContactsSchema;
import com.murphy.pokotalk.data.file.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.file.schema.GroupsSchema;
import com.murphy.pokotalk.data.file.schema.MessagesSchema;
import com.murphy.pokotalk.data.file.schema.SessionSchema;
import com.murphy.pokotalk.data.file.schema.UsersSchema;

public class PokoDatabase  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DB_NAME = "POKOTALK_DB";
    protected static PokoDatabase instance = null;

    public synchronized static PokoDatabase getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new PokoDatabase(context);
        }

        return instance;
    }

    public PokoDatabase(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        // Create tables if not exists
        db.beginTransaction();
        try {
            db.execSQL(UsersSchema.SQL_CREATE_TABLE);
            db.execSQL(SessionSchema.SQL_CREATE_TABLE);
            db.execSQL(GroupsSchema.SQL_CREATE_TABLE);
            db.execSQL(ContactsSchema.SQL_CREATE_TABLE);
            db.execSQL(GroupMembersSchema.SQL_CREATE_TABLE);
            db.execSQL(MessagesSchema.SQL_CREATE_TABLE);

            db.setTransactionSuccessful();
            Log.v("POKO", "Make sure schema made");
        } catch (Exception e) {
            // Failed to initialize database
            e.printStackTrace();
            instance = null;
            Log.v("POKO", "Failed to initialize DB");
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We do not support database upgrade yet
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We do not support database downgrade yet
    }

    @Override
    public synchronized void close() {
        instance = null;

        super.close();
    }
}
