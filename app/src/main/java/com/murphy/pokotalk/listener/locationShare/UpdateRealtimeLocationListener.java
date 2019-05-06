package com.murphy.pokotalk.listener.locationShare;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateRealtimeLocationListener extends PokoServer.PokoListener {
    public UpdateRealtimeLocationListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.updateRealtimeLocationName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            if (jsonObject.has("eventId")) {
                int eventId = jsonObject.getInt("eventId");

                Log.v("POKO ERROR", "Updated location " + eventId);

                putData("eventId", eventId);
            }
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad update location data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to update location");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
