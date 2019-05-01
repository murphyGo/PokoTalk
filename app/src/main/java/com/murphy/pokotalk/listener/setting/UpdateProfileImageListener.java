package com.murphy.pokotalk.listener.setting;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.schema.SessionSchema;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.content.ContentTransferManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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

                putData("picture", contentName);
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
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            String picture = (String) data.get("picture");
            Contact user = Session.getInstance().getUser();

            if (picture == null || user == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE PICTURE DATA " +
                    user.getNickname() + ", " + user.getUserId());

            /* Save user picture data */
            SQLiteDatabase db = getWritableSessionDatabase();

            if (db == null) {
                return;
            }

            // Put picture data
            ContentValues values = new ContentValues();
            values.put(SessionSchema.Entry.PICTURE, picture);

            // Start a transaction
            db.beginTransaction();
            try {
                // Update session data
                PokoDatabaseHelper.updateSessionData(db, user, values);

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE PICTURE DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save picture data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
