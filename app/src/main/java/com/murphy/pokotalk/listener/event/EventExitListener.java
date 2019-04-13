package com.murphy.pokotalk.listener.event;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EventExitListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.eventExitName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        try {
            JSONObject data = (JSONObject) args[0];
            DataCollection collection = DataCollection.getInstance();
            EventList eventList = collection.getEventList();
            int eventId = data.getInt("eventId");

            if (eventList.removeItemByKey(eventId) == null) {
                Log.e("POKO ERROR", "No such event to remove with this event id");
            }

            putData("eventId", eventId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad event exit json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get created event");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Integer eventId = (Integer) data.get("eventId");

            if (eventId == null) {
                return;
            }

            Log.v("POKO", "START TO erase event DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Set foreign key constraint enabled so that members and messages are also deleted.
            db.setForeignKeyConstraintsEnabled(true);

            // Start a transaction
            db.beginTransaction();
            try {
                String[] selectionArgs = {Integer.toString(eventId)};

                // Insert or update event data
                PokoDatabaseHelper.deleteEventData(db, selectionArgs);

                db.setTransactionSuccessful();
                Log.v("POKO", "erased event data successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to erase event data");
            } finally {
                // End a transaction
                db.endTransaction();

                // Set foreign key constraint disabled.
                db.setForeignKeyConstraintsEnabled(false);

                db.releaseReference();
            }
        }
    }
}
