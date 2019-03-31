package com.murphy.pokotalk.listener.event;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
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
        return null;
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {

        }
    }
}
