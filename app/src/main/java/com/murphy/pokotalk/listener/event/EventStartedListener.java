package com.murphy.pokotalk.listener.event;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EventStartedListener extends PokoServer.PokoListener {
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
            PokoEvent paredEvent = PokoParser.parseEvent(jsonObject);

            // Get event
            PokoEvent event = eventList.getItemByKey(paredEvent.getEventId());
            if (event == null) {
                Log.e("POKO ERROR", "Can't start event since there is no such event.");
                return;
            }

            // Update event state
            event.setState(PokoEvent.EVENT_STARTED);

            putData("event", event);
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
        return null;
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {

        }
    }
}
