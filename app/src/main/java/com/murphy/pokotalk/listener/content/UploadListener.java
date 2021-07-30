package com.murphy.pokotalk.listener.content;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadListener extends PokoServer.PokoListener {
    public UploadListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.uploadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("uploadId")) {
                // Get upload id and ack
                int uploadId = data.getInt("uploadId");
                int ack = data.getInt("ack");

                // Update ack
                ContentTransferManager.getInstance().uploadAck(uploadId, ack);
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
                // Get upload id and ack
                int uploadId = data.getInt("uploadId");

                // Failed to upload, clean up
                ContentTransferManager.getInstance().failUploadJob(uploadId, false);

                Log.e("POKO ERROR", "Failed to upload");
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse upload");
        }
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
