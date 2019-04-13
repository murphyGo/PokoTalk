package com.murphy.pokotalk.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.Contact;

import java.util.HashMap;

public abstract class PokoAsyncDatabaseJob
        extends AsyncTask<HashMap<String, Object>, Void, Void> {
    protected PokoSessionDatabase pokoSessionDatabase;
    protected PokoUserDatabase pokoUserDatabase;

    protected static boolean enabled = true;

    @Override
    protected synchronized Void doInBackground(HashMap<String, Object>... args) {
        PokoTalkApp app = PokoTalkApp.getInstance();
        Context context = app.getApplicationContext();

        /* Acquire DB job lock here.
         * Lock acquire order is DataLock -> Database lock.
         * So Database jobs are always executed in order of
         * DataLock acquisition, so that we can avoid execution order
         * change. */
        try {
            DataLock.getDatabaseJobInstance().acquireWriteLock();

            try {
                // Get user
                Contact user = Session.getInstance().getUser();

                if (user != null) {
                    // Get user id
                    int userId = Session.getInstance().getUser().getUserId();

                    // Get user database
                    pokoUserDatabase = PokoUserDatabase.getInstance(context, userId);
                }

                // Get session database
                pokoSessionDatabase = PokoSessionDatabase.getInstance(context);

                // Do database job
                doJob(args[0]);
            } finally {
                /* We finally release database lock */
                //Session.checkSessionData();
                DataLock.getDatabaseJobInstance().releaseWriteLock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            /* Job is done, let job manager can start next job */
            PokoDatabaseManager.getInstance().startNextJob();
        }

        return null;
    }

    protected SQLiteDatabase getWritableDatabase() {
        if (pokoUserDatabase == null) {
            return null;
        }

        return pokoUserDatabase.getWritableDatabase();
    }

    protected SQLiteDatabase getReadableDatabase() {
        if (pokoUserDatabase == null) {
            return null;
        }

        return pokoUserDatabase.getReadableDatabase();
    }

    protected SQLiteDatabase getWritableSessionDatabase() {
        if (pokoSessionDatabase == null) {
            return null;
        }

        return pokoSessionDatabase.getWritableDatabase();
    }

    protected SQLiteDatabase getReadableSessionDatabase() {
        if (pokoSessionDatabase == null) {
            return null;
        }

        return pokoSessionDatabase.getReadableDatabase();
    }

    protected abstract void doJob(HashMap<String, Object> data);
}