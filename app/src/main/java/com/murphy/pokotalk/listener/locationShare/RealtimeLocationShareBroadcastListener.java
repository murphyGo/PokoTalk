package com.murphy.pokotalk.listener.locationShare;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.locationShare.LocationShareHelper;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RealtimeLocationShareBroadcastListener extends PokoServer.PokoListener {
    public RealtimeLocationShareBroadcastListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.realtimeLocationShareBroadcastName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            if (jsonObject.has("id") && jsonObject.has("locations")
                    && jsonObject.has("timestamp")) {
                int eventId = jsonObject.getInt("id");
                JSONArray locations = jsonObject.getJSONArray("locations");
                Calendar calendar = Parser.epochInMillsToCalendar(
                        jsonObject.getLong("timestamp"));

                Log.v("POKO ERROR", "Get location broadcast " + eventId);

                LocationShareHelper.getInstance().updateLocations(eventId, locations, calendar);

                putData("eventId", eventId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad location share broadcast data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get location share broadcast");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
