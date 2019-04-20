package com.murphy.pokotalk.listener.setting;

import android.content.Context;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateProfileImageListener extends PokoServer.PokoListener {
    public UpdateProfileImageListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.updateProfileImageName;
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
            } else if (data.has("contentName")) {
                // Upload is done, get content name
                String contentName = data.getString("contentName");

                // Get user
                Contact user = Session.getInstance().getUser();

                // Update user profile
                user.setPicture(contentName);
            }
        } catch (JSONException e) {
            Log.e("POKO", "Failed to parse profile image update response");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to update profile");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
