package com.murphy.pokotalk.listener.chat;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReadMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.readMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupPokoList groupList = collection.getGroupList();
        try {
            JSONArray messages = data.getJSONArray("messages");
            int groupId = data.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read messages since there is no such group.");
                return;
            }

            /* Parse all message and sort in time */
            MessagePokoList messageList = group.getMessageList();
            ArrayList<PokoMessage> readMessages = new ArrayList<>();

            for (int i = 0; i < messages.length(); i++) {
                JSONObject jsonMessage = messages.getJSONObject(i);
                PokoMessage message = PokoParser.parseMessage(jsonMessage);
                // Update item and assign message the message updated in the list.
                message = messageList.updateItem(message);
                readMessages.add(message);

                /* If the message is history, send get history */
                if (message.getSpecialContent() == null
                        && message.getMessageType() == PokoMessage.MEMBER_JOIN) {
                    PokoServer server = PokoServer.getInstance();
                    if (server != null) {
                        server.sendGetMemberJoinHistory(groupId, message.getMessageId());
                    }
                }
            }

            messageList.sortItemsByKey();

            putData("group", group);
            putData("messages", readMessages);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad read message json data");
            e.printStackTrace();
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to read messages");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");
            ArrayList<PokoMessage> messages = (ArrayList<PokoMessage>) data.get("messages");

            if (group == null || messages == null) {
                return;
            }

            Log.v("POKO", "START TO save messages DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                for (PokoMessage message : messages) {
                    ContentValues values = Serializer.obtainMessageValues(group, message);

                    // Add message data
                    PokoDatabaseHelper.insertOrIgnoreMessageData(db, values);
                }

                // Update nbNewMessage of group
                PokoDatabaseHelper.updateGroupNbNewMessage(db, group);

                db.setTransactionSuccessful();
                Log.v("POKO", "saved messages successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save messages data.");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
