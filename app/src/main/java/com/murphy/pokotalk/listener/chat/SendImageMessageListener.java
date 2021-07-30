package com.murphy.pokotalk.listener.chat;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.content.ContentTransferManager;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class SendImageMessageListener extends PokoServer.PokoListener {
    public SendImageMessageListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.sendImageMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("uploadId") && data.has("sendId")) {
                // Get send id and upload id
                int uploadId = data.getInt("uploadId");
                int sendId = data.getInt("sendId");

                // Start uploading profile image
                ContentTransferManager.getInstance().startUploadJob(sendId, uploadId);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse image message response");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to send image message");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
