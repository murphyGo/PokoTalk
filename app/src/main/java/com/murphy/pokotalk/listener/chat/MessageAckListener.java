package com.murphy.pokotalk.listener.chat;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MessageAckListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.messageAckName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();
        try {
            JSONObject jsonMessage = data.getJSONObject("message");
            int groupId = jsonMessage.getInt("groupId");
            int userId =  jsonMessage.getInt("userId");
            int ackStart = jsonMessage.getInt("ackStart");
            int ackEnd = jsonMessage.getInt("ackEnd");

            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't ack message since there is no such group.");
                return;
            }

            User user = group.getMembers().getItemByKey(userId);
            if (user == null) {
                Log.e("POKO ERROR", "Can't ack message since there is no such user.");
                return;
            }

            /* Decrement nbNotReadUser of acked messages */
            MessageList messageList = group.getMessageList();
            messageList.ackMessages(ackStart, ackEnd, true, user);

            putData("group",group);
            putData("ackStart", ackStart);
            putData("ackEnd", ackEnd);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad message ack json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new message");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");
            Integer ackStart = (Integer) data.get("ackStart");
            Integer ackEnd = (Integer) data.get("ackEnd");

            if (group == null || ackStart == null || ackEnd == null) {
                return;
            }

            if (ackEnd < ackStart) {
                return;
            }

            Log.v("POKO", "START TO update message ack DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Decrement messages ack
                PokoDatabaseHelper.decrementMessageAck(db, group, ackStart, ackEnd);

                db.setTransactionSuccessful();
                Log.v("POKO", "updated message ack successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to update message ack data.");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
