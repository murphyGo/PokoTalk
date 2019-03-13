package com.murphy.pokotalk.listener.chat;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AckMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.ackMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        DataCollection collection = DataCollection.getInstance();
        try {
            JSONObject jsonObject = (JSONObject) args[0];
            int groupId = jsonObject.getInt("groupId");
            int toId = jsonObject.getInt("ackEnd");

            Group group = collection.getGroupList().getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "can't ack message since there's no such group.");
                return;
            }

            group.setAck(Math.max(group.getAck(), toId));

            putData("group", group);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "bad ack message json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to ack message");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");

            if (group == null) {
                return;
            }

            Log.v("POKO", "START TO update group ack DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Update ack of group
                PokoDatabaseHelper.updateGroupAck(db, group);

                db.setTransactionSuccessful();
                Log.v("POKO", "updated group ack successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to update group ack data.");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
