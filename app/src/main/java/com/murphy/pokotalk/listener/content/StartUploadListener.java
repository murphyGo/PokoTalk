package com.murphy.pokotalk.listener.content;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class StartUploadListener extends PokoServer.PokoListener {
    public StartUploadListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.startUploadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("uploadId") && data.has("contentName")) {
                // Get upload id and content name
                int uploadId = data.getInt("uploadId");
                String contentName = data.getString("contentName");

                // Start transfer data
                ContentTransferManager.getInstance().uploadJob(uploadId, contentName);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse upload start");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("uploadId")) {
                // Get upload id
                int uploadId = data.getInt("uploadId");

                // Failed to upload, clean up upload job
                ContentTransferManager.getInstance().failUploadJob(uploadId, false);

                Log.e("POKO ERROR", "Failed to start upload content");
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse upload start");
        }
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
