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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GetEventListListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.getEventListName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        EventList list = DataCollection.getInstance().getEventList();
        try {
            list.startUpdateList();
            JSONArray events = data.getJSONArray("events");
            for (int i = 0; i < events.length(); i++) {
                JSONObject jsonObject = events.getJSONObject(i);
                /* Parse event */
                PokoEvent event = PokoParser.parseEvent(jsonObject);
                list.updateItem(event);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad event list json data");
        } finally {
            list.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get event list");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            EventList eventList = DataCollection.getInstance().getEventList();
            Log.v("POKO", "START TO WRITE event list DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Delete all event data
                PokoDatabaseHelper.deleteAllEventData(db);

                // Insert or update all event data
                for (PokoEvent event : eventList.getList()) {
                    PokoDatabaseHelper.insertOrUpdateEventData(db, event);
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE event list DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save event list data");
                e.printStackTrace();
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
