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

public class ExitRealtimeLocationShareListener extends PokoServer.PokoListener {
    public ExitRealtimeLocationShareListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.exitRealtimeLocationShareName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            if (jsonObject.has("eventId")) {
                int eventId = jsonObject.getInt("eventId");

                Log.v("POKO ERROR", "Exited location share " + eventId);

                // Remove room
                LocationShareHelper.getInstance().removeRoom(eventId);

                putData("eventId", eventId);
            }
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad exit location share data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to exit location share");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
