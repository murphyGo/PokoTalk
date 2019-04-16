package com.murphy.pokotalk.listener.content;

import android.util.Base64;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class DownloadListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.downloadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("downloadId")
                    && data.has("buffer")
                    && data.has("size")) {
                // Get download id and size
                int downloadId = data.getInt("downloadId");
                int size = data.getInt("size");

                // Get buffer
                String buffer = data.getString("buffer");

                // Decode to bytes
                byte[] decodedBuffer = Base64.decode(buffer, Base64.DEFAULT);

                // Receive data
                ContentTransferManager.getInstance().downloadJob(downloadId, decodedBuffer);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse downloaded data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to download content");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
