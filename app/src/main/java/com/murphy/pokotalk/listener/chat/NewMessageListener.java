package com.murphy.pokotalk.listener.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.ChatManager;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.group.MessagePokoList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;

public class NewMessageListener extends PokoServer.PokoListener {
    public NewMessageListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.newMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        Log.v("POKO", "NEW MESSAGE CALLBACK");
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupPokoList groupList = collection.getGroupList();
        try {
            JSONObject jsonMessage = data.getJSONObject("message");
            int groupId = jsonMessage.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read message since there is no such group.");
                return;
            }

            /* Parse message and add sorted by message id */
            MessagePokoList messageList = group.getMessageList();
            PokoMessage message = PokoParser.parseMessage(jsonMessage);

            // Update item and assign message the message updated in the list.
            message = messageList.updateItem(message);

            /* Add NbNewMessage number */
            /* Increment new message number only when the user is not chatting for this group */
            if (ChatManager.getChattingGroup() != group) {
                group.setNbNewMessages(group.getNbNewMessages() + 1);
            }

            /* If the message is history, send get history */
            if (message.getSpecialContent() == null
                    && message.getMessageType() == PokoMessage.TYPE_MEMBER_JOIN) {
                PokoServer.getInstance().sendGetMemberJoinHistory(groupId, message.getMessageId());
            }

            Log.v("POKO", "NEW MESSAGE GROUP ID " + group.getGroupId());
            putData("group", group);
            putData("message", messageList.getItemByKey(message.getMessageId()));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new message json data");
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
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
            PokoMessage message = (PokoMessage) data.get("message");

            if (group == null || message == null) {
                return;
            }

            Log.v("POKO", "START TO save message DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Get content value of message
                ContentValues values = Serializer.obtainMessageValues(group, message);

                // Add message data
                PokoDatabaseHelper.insertOrIgnoreMessageData(db, values);

                // Update nbNewMessage of group
                PokoDatabaseHelper.updateGroupNbNewMessage(db, group);

                db.setTransactionSuccessful();
                Log.v("POKO", "saved message successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save message data.");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
