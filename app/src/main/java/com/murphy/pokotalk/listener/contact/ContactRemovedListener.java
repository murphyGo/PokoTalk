package com.murphy.pokotalk.listener.contact;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ContactRemovedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.contactRemovedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        try {
            /* The user becomes now stranger */
            int userId = data.getInt("userId");
            collection.moveUserToStrangerList(userId);

            putData("userId", userId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get removed contact data");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Integer userId = (Integer) data.get("userId");

            if (userId == null) {
                return;
            }

            Log.v("POKO", "START TO write contact removed");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            try {
                // delete contact data
                String[] selectionArgs = {Integer.toString(userId)};
                long count = PokoDatabaseHelper.deleteContactData(db, selectionArgs);
                if (count <= 0) {
                    Log.e("POKO", "Can not remove contact from db(No such contact)");
                }
                Log.v("POKO", "write contact removed successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to write contact removed data");
            }
        }
    }
}
