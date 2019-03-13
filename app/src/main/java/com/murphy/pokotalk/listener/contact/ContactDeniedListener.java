package com.murphy.pokotalk.listener.contact;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ContactDeniedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.contactDeniedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        try {
            JSONObject jsonObject = data.getJSONObject("contact");
            PendingContact contact = PokoParser.parsePendingContact(jsonObject);
            /* The user becomes now stranger */
            collection.moveUserToStrangerList(contact.getUserId());

            putData("userId", contact.getUserId());
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get contact denied data");
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

            Log.v("POKO", "START TO write pending contact removed");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            try {
                // delete contact data
                String[] selectionArgs = {Integer.toString(userId)};
                long count = PokoDatabaseHelper.deleteContactData(db, selectionArgs);
                if (count <= 0) {
                    Log.e("POKO", "Can not remove pending contact from db(No such contact)");
                }
                Log.v("POKO", "write pending contact removed successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to write pending contact removed data");
            }
        }
    }
}
