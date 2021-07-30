package com.murphy.pokotalk.listener.group;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;

public class GetGroupListListener extends PokoServer.PokoListener {
    public GetGroupListListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.getGroupListName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        GroupList list = DataCollection.getInstance().getGroupList();
        try {
            list.startUpdateList();
            JSONArray groups = data.getJSONArray("groups");
            for (int i = 0; i < groups.length(); i++) {
                JSONObject jsonObject = groups.getJSONObject(i);
                /* Parse group */
                Group group = PokoParser.parseGroup(jsonObject);
                list.updateItem(group);

                /* Parse last message */
                if (jsonObject.has("lastMessage")) {
                    JSONObject jsonLastMessage = jsonObject.getJSONObject("lastMessage");
                    PokoMessage lastMessage = PokoParser.parseMessage(jsonLastMessage);
                    group = list.getItemByKey(group.getGroupId());
                    MessageList messageList = group.getMessageList();

                    // Update item and assign message the message updated in the list.
                    lastMessage = messageList.updateItem(lastMessage);

                    /* If the message is history, send get history */
                    if (lastMessage.getSpecialContent() == null &&
                            lastMessage.getMessageType() == PokoMessage.TYPE_MEMBER_JOIN) {
                        PokoServer.getInstance().sendGetMemberJoinHistory(group.getGroupId(),
                                lastMessage.getMessageId());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad group json data");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad last message data");
        } finally {
            list.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get group list");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            GroupList groupList = DataCollection.getInstance().getGroupList();
            Log.v("POKO", "START TO WRITE group list DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Delete all group data
                PokoDatabaseHelper.deleteAllGroupData(db);

                // Insert or update all group data
                for (Group group : groupList.getList()) {
                    PokoDatabaseHelper.insertOrUpdateGroupData(db, group);
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE group list DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save group list data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
