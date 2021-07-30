package com.murphy.pokotalk.data.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.schema.ContactsSchema;
import com.murphy.pokotalk.data.db.schema.EventLocationSchema;
import com.murphy.pokotalk.data.db.schema.EventParticipantsSchema;
import com.murphy.pokotalk.data.db.schema.EventsSchema;
import com.murphy.pokotalk.data.db.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.db.schema.GroupsSchema;
import com.murphy.pokotalk.data.db.schema.MessagesSchema;
import com.murphy.pokotalk.data.db.schema.UsersSchema;

public class PokoUserDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DB_NAME_FORMAT = "POKOTALK_DB%d";
    public String DB_NAME;
    protected static PokoUserDatabase instance = null;

    protected static SparseArray<PokoUserDatabase> databasePool = new SparseArray<>();

    public static PokoUserDatabase getInstance(Context context, int userId) {
        PokoUserDatabase instance;

        synchronized (PokoUserDatabase.class) {
            instance = databasePool.get(userId);

            if (instance != null) {
                return instance;
            }

            instance = new PokoUserDatabase(context, userId);
            databasePool.put(userId, instance);
        }

        return instance;
    }

    public PokoUserDatabase(Context context, int userId) {
        super(context, String.format(Constants.locale, PokoUserDatabase.DB_NAME_FORMAT, userId),
                null, DATABASE_VERSION);

        DB_NAME = String.format(Constants.locale, PokoUserDatabase.DB_NAME_FORMAT, userId);
    }

    // This method is called only once for application lifetime.
    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        Log.v("POKO", "POKO DB CREATE");

        db.beginTransaction();
        db.execSQL(UsersSchema.SQL_CREATE_TABLE);
        db.execSQL(GroupsSchema.SQL_CREATE_TABLE);
        db.execSQL(ContactsSchema.SQL_CREATE_TABLE);
        db.execSQL(GroupMembersSchema.SQL_CREATE_TABLE);
        db.execSQL(MessagesSchema.SQL_CREATE_TABLE);
        db.execSQL(EventsSchema.SQL_CREATE_TABLE);
        db.execSQL(EventParticipantsSchema.SQL_CREATE_TABLE);
        db.execSQL(EventLocationSchema.SQL_CREATE_TABLE);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void makeSureTableExists() {
        SQLiteDatabase db = getWritableDatabase();

        // Create tables if not exists
        db.beginTransaction();
        try {
            db.execSQL(UsersSchema.SQL_CREATE_TABLE);
            db.execSQL(GroupsSchema.SQL_CREATE_TABLE);
            db.execSQL(ContactsSchema.SQL_CREATE_TABLE);
            db.execSQL(GroupMembersSchema.SQL_CREATE_TABLE);
            db.execSQL(MessagesSchema.SQL_CREATE_TABLE);
            db.execSQL(EventsSchema.SQL_CREATE_TABLE);
            db.execSQL(EventParticipantsSchema.SQL_CREATE_TABLE);
            db.execSQL(EventLocationSchema.SQL_CREATE_TABLE);

            db.setTransactionSuccessful();
            Log.v("POKO", "Make sure tables exists");
        } catch (Exception e) {
            // Failed to initialize database
            e.printStackTrace();
            instance = null;
            Log.v("POKO", "Failed to initialize DB");
        } finally {
            db.endTransaction();

            db.close();
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
