package com.murphy.pokotalk.listener.content;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class StartDownloadListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.startDownloadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("downloadId")
                    && data.has("sendId")
                    && data.has("size")) {
                // Get download id and send Id
                int downloadId = data.getInt("downloadId");
                int sendId = data.getInt("sendId");

                // Get size
                int size = data.getInt("size");

                // Start receive data
                ContentTransferManager.getInstance().startDownloadJob(sendId, downloadId, size);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse download start");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to start download");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
