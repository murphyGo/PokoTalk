package com.murphy.pokotalk.listener.event;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
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

        }
    }
}
