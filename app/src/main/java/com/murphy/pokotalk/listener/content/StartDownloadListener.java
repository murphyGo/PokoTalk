package com.murphy.pokotalk.listener.content;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.content.ContentLoadService;
import com.murphy.pokotalk.server.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class StartDownloadListener extends PokoServer.PokoListener {
    public StartDownloadListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.startDownloadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        Context context = (Context) getData("context");

        try {
            if (data.has("downloadId")
                    && data.has("sendId")
                    && data.has("size")) {
                // Get download id and send Id
                int downloadId = data.getInt("downloadId");
                int sendId = data.getInt("sendId");

                // Get size
                int size = data.getInt("size");

                // Make intent to start service
                Intent intent = new Intent(context, ContentLoadService.class);
                intent.putExtra("command", ContentLoadService.CMD_START_DOWNLOAD);
                intent.putExtra("downloadId", downloadId);
                intent.putExtra("sendId", sendId);
                intent.putExtra("size", size);

                // Start service to start download
                context.startService(intent);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse download start");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("sendId")) {
                // Get send id
                int sendId = data.getInt("sendId");

                // Failed to upload, clean up upload job
                ContentTransferManager.getInstance().failDownloadJob(sendId, true);

                Log.e("POKO ERROR", "Failed to start download");
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse download start");
        }
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
