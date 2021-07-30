package com.murphy.pokotalk.listener.event;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EventStartedListener extends PokoServer.PokoListener {
    public EventStartedListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.eventStartedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        EventList eventList = collection.getEventList();

        try {
            if (!jsonObject.has("eventId") || !jsonObject.has("groupId")) {
                return;
            }

            // Get event id and group id
            int eventId = jsonObject.getInt("eventId");
            int groupId = jsonObject.getInt("groupId");

            // Get event
            PokoEvent event = eventList.getItemByKey(eventId);
            if (event == null) {
                Log.e("POKO ERROR", "Can't start event since there is no such event.");
                return;
            }

            // Update event state and group
            event.setState(PokoEvent.EVENT_STARTED);

            // Put event group relation
            collection.getEventList().putEventGroupRelation(eventId, groupId);

            putData("event", event);
            putData("groupId", groupId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad event started json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get event stated");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            PokoEvent event = (PokoEvent) data.get("event");
            Integer groupId = (Integer) data.get("groupId");

            if (event == null || groupId == null) {
                return;
            }

            Log.v("POKO", "START TO write event started DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Update event ack data
                if (PokoDatabaseHelper.updateEventStarted(db, event, PokoEvent.EVENT_STARTED) == 0) {
                    Log.e("POKO", "Failed to update event started, no such event");
                }

                if (PokoDatabaseHelper.updateEventGroup(db, event, groupId) == 0) {
                    Log.e("POKO", "Failed to update event group, no such event");
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "Write event started data successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to write event started data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
