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

public class DownloadListener extends PokoServer.PokoListener {
    public DownloadListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.downloadName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        Context context = (Context) getData("context");

        try {
            if (data.has("downloadId")
                    && data.has("buffer")
                    && data.has("size")) {
                // Get download id and size
                int downloadId = data.getInt("downloadId");
                int size = data.getInt("size");

                // Get buffer
                byte[] buffer = (byte[]) data.get("buffer");

                // Put downloaded bytes to queue of download job
                ContentTransferManager.getInstance().putBytesToJobQueue(downloadId, buffer);

                // Make intent to copy buffer in service
                Intent intent = new Intent(context, ContentLoadService.class);
                intent.putExtra("command", ContentLoadService.CMD_DOWNLOAD);
                intent.putExtra("downloadId", downloadId);

                // Start service
                context.startService(intent);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse downloaded data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];

        try {
            if (data.has("downloadId")) {
                // Get send id
                int downloadId = data.getInt("downloadId");

                // Failed to upload, clean up upload job
                ContentTransferManager.getInstance().failDownloadJob(downloadId, false);

                Log.e("POKO ERROR", "Failed to download content");
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse download");
        }
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
