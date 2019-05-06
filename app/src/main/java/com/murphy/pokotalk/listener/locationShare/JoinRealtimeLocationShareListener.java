package com.murphy.pokotalk.listener.locationShare;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.locationShare.LocationShareHelper;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinRealtimeLocationShareListener extends PokoServer.PokoListener {
    public JoinRealtimeLocationShareListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.joinRealtimeLocationShareName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            if (jsonObject.has("eventId") && jsonObject.has("location")
                && jsonObject.has("number")) {
                int eventId = jsonObject.getInt("eventId");
                JSONObject jsonLocation = jsonObject.getJSONObject("location");
                int number = jsonObject.getInt("number");

                Log.v("POKO", "Joined location share " + eventId);

                // Put meeting location and my location number
                LocationShareHelper.getInstance()
                        .setRoomJoined(eventId, number, jsonLocation);

                putData("eventId", eventId);
                putData("number", number);
            }
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad join location share data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to join location share");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
