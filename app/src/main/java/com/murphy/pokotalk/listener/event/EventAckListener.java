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

public class EventAckListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.eventAckName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        EventList eventList = collection.getEventList();
        try {
            int eventId = jsonObject.getInt("eventId");
            int ack =  PokoParser.parseEventAck(jsonObject);

            // Get event
            PokoEvent event = eventList.getItemByKey(eventId);
            if (event == null) {
                Log.e("POKO ERROR", "Can't ack event since there is no such event.");
                return;
            }

            // Update ack
            event.setAck(ack);

            putData("event", event);
            putData("ack", ack);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad event ack json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get event ack");
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
