package com.murphy.pokotalk.listener.content;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.content.ContentTransferManager;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.service.ContentService;

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

        // Get context
        Context context = (Context) getData("context");

        try {
            if (data.has("uploadId") && data.has("contentName")) {
                // Get upload id and content name
                int uploadId = data.getInt("uploadId");
                String contentName = data.getString("contentName");

                // Set content name
                ContentTransferManager.getInstance().setUploadJobContentName(uploadId, contentName);

                // Make intent to start service
                Intent intent = new Intent(context, ContentService.class);
                intent.putExtra("command", ContentService.CMD_UPLOAD);
                intent.putExtra("uploadId", uploadId);

                // Start service to start upload
                context.startService(intent);
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
