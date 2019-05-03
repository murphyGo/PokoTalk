
package com.murphy.pokotalk.listener.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ReadNbreadOfMessages extends PokoServer.PokoListener {
    public ReadNbreadOfMessages(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.readNbreadOfMessages;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupPokoList groupList = collection.getGroupList();
        try {
            JSONArray jsonNbNotReads = data.getJSONArray("messages");
            int groupId = data.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read nbreads since there is no such group.");
                return;
            }

            /* Parse all message and sort in time */
            MessageList messageList = group.getMessageList();

            for (int i = 0; i < jsonNbNotReads.length(); i++) {
                JSONObject jsonNbNotRead = jsonNbNotReads.getJSONObject(i);
                int messageId = jsonNbNotRead.getInt("messageId");
                int nbNotRead = jsonNbNotRead.getInt("nbread");
                if (nbNotRead < 0) {
                    continue;
                }

                PokoMessage message = messageList.getItemByKey(messageId);
                if (message != null) {
                    message.setNbNotReadUser(nbNotRead);
                }
            }

            putData("group", group);
            putData("nbNotReads", jsonNbNotReads);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad read nbread of message json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to read nbread of messages");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");
            JSONArray jsonNbNotReads = (JSONArray) data.get("nbNotReads");

            if (group == null || jsonNbNotReads == null) {
                return;
            }

            Log.v("POKO", "START TO update message ack DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Update ack for messages
                for (int i = 0; i < jsonNbNotReads.length(); i++) {
                    JSONObject jsonNbNotRead = jsonNbNotReads.getJSONObject(i);
                    int messageId = jsonNbNotRead.getInt("messageId");
                    int nbNotRead = jsonNbNotRead.getInt("nbread");
                    PokoDatabaseHelper.updateMessageAck(db, group, messageId, nbNotRead);
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "updated message ack successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to update message ack data.");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}

