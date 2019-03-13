package com.murphy.pokotalk.listener.chat;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GetMemberJoinHistory extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.getMemberJoinHistory;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();

        try {
            int groupId = data.getInt("groupId");
            int messageId = data.getInt("messageId");
            JSONArray jsonMembers = data.getJSONArray("members");

            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't get member join history" +
                        " since there is no such group.");
                return;
            }

            PokoMessage message = group.getMessageList().getItemByKey(messageId);
            if (message == null) {
                Log.e("POKO ERROR", "Can't get member join history" +
                        " since there is no such message.");
                return;
            }

            StringBuffer nicknames = new StringBuffer();
            nicknames.append(message.getWriter().getNickname() + "님이 ");

            for (int i = 0; i < jsonMembers.length(); i++) {
                JSONObject jsonMember = jsonMembers.getJSONObject(i);
                String nickname = jsonMember.getString("nickname");
                nicknames.append(nickname);
                if (i < jsonMembers.length() - 1) {
                    nicknames.append(", ");
                }
            }

            nicknames.append("님을 초대하셨습니다.");
            message.setSpecialContent(nicknames.toString());

            putData("group", group);
            putData("message", message);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad message ack json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get member join history");
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

            Log.v("POKO", "START TO update member join DATA.");

            /* Get a database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Update special content of the message
                PokoDatabaseHelper.updateSpacialContentsOfMessage(db, group, message);

                db.setTransactionSuccessful();
                Log.v("POKO", "updated member join data successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to update member join data.");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
