package com.murphy.pokotalk.listener.chat;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class SendMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.sendMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        try {
            int groupId = data.getInt("groupId");
            int messageId = data.getInt("messageId");
            int sendId = data.getInt("sendId");
            int nbNotRead = data.getInt("nbread");
            Calendar date = Parser.epochInMillsToCalendar(data.getLong("date"));

            Group group = collection.getGroupList().getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Cannot send message since no such contact");
                return;
            }

            MessageList messageList = group.getMessageList();
            if (!messageList.moveSentMessageToMessageList(
                    sendId, messageId, nbNotRead, date)) {
                Log.e("POKO ERROR", "Failed to send message to message list");
                return;
            }

            putData("group", group);
            putData("message", messageList.getItemByKey(messageId));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad send message json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to send message");
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

            Log.v("POKO", "START TO save my message DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Get content value of message
                ContentValues values = Serializer.obtainMessageValues(group, message);

                // Add message data
                PokoDatabaseHelper.insertOrIgnoreMessageData(db, values);

                db.setTransactionSuccessful();
                Log.v("POKO", "saved my message successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save my message data.");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
