package com.murphy.pokotalk.data.file;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;

import java.util.HashMap;

public abstract class PokoAsyncDatabaseJob
        extends AsyncTask<HashMap<String, Object>, Void, Void> {
    protected PokoDatabase pokoDatabase;
    @Override
    protected synchronized Void doInBackground(HashMap<String, Object>... args) {
        /* Acquire DB job lock here.
         * Lock acquire order is DataLock -> Database lock.
         * So Database jobs are always executed in order of
         * DataLock acquisition, so that we can avoid execution order
         * change. */
        try {
            DataLock.getDatabaseJobInstance().acquireWriteLock();

            try {
                pokoDatabase = PokoDatabase.getInstance(null);
                if (pokoDatabase == null) {
                    return null;
                }

                doJob(args[0]);
            } finally {
                /* We finally release database lock */
                Session.checkSessionData();
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
        return pokoDatabase.getWritableDatabase();
    }

    protected SQLiteDatabase getReadableDatabase() {
        return pokoDatabase.getReadableDatabase();
    }

    protected abstract void doJob(HashMap<String, Object> data);
}