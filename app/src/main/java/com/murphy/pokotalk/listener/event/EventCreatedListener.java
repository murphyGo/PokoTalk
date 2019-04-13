package com.murphy.pokotalk.listener.event;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EventCreatedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.eventCreatedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonEvent = (JSONObject) args[0];
        EventList list = DataCollection.getInstance().getEventList();
        try {
            // Parse event
            PokoEvent event = PokoParser.parseEvent(jsonEvent);

            // Update event
            event = list.updateItem(event);

            putData("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad event json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get created event");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            PokoEvent event = (PokoEvent) data.get("event");

            if (event == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE event DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Insert or update event data
                PokoDatabaseHelper.insertOrUpdateEventData(db, event);

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE event data successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save event data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
