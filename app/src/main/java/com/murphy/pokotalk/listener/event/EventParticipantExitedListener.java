package com.murphy.pokotalk.listener.event;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserPokoList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EventParticipantExitedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.eventParticipantExitedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            int eventId = jsonObject.getInt("eventId");
            int userId = jsonObject.getInt("userId");
            PokoEvent event = DataCollection.getInstance().getEventList().getItemByKey(eventId);

            /* Event should exist */
            if (event == null) {
                Log.e("POKO ERROR", "Participant cannot exit since there is no such event");
                return;
            }

            UserPokoList participants = event.getParticipants();
            User participant = participants.removeItemByKey(userId);
            if (participant == null) {
                Log.e("POKO ERROR", "Participant cannot exit since there is no such user");
            }

            putData("event", event);
            putData("participant", participant);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad exit participant json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get participant exited data");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            PokoEvent event = (PokoEvent) data.get("event");
            User participant = (User) data.get("participant");

            if (event == null || participant == null) {
                return;
            }

            Log.v("POKO", "START TO erase event participant DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                String[] selectionArgs = {Integer.toString(event.getEventId()),
                        Integer.toString(participant.getUserId())};

                // Update event ack data
                if (PokoDatabaseHelper.deleteEventParticipantData(db, selectionArgs) == 0) {
                    Log.e("POKO", "Failed to erase event participant, no such event participant");
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "Erase event participant data successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to erase event participant data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
