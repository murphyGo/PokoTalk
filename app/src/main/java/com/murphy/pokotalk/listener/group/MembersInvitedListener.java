package com.murphy.pokotalk.listener.group;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MembersInvitedListener extends PokoServer.PokoListener {
    public MembersInvitedListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.membersInvitedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject jsonObject = (JSONObject) args[0];

        try {
            int groupId = jsonObject.getInt("groupId");
            JSONArray jsonMembers = jsonObject.getJSONArray("members");
            Group group = DataCollection.getInstance().getGroupList().getItemByKey(groupId);

            /* Group should exist */
            if (group == null) {
                Log.e("POKO ERROR", "Member cannot enter group since there is no such group");
                return;
            }

            DataCollection collection = DataCollection.getInstance();
            StrangerList strangerList = collection.getStrangerList();
            UserList memberList = group.getMembers();
            ArrayList<User> members = new ArrayList<>();
            for (int i = 0; i < jsonMembers.length(); i++) {
                JSONObject jsonMember = jsonMembers.getJSONObject(i);
                Stranger invitedStranger = PokoParser.parseStranger(jsonMember);
                User existingUser = collection.getUserById(invitedStranger.getUserId());
                /* If the user exists just add to member list */
                if (existingUser != null) {
                    memberList.updateItem(existingUser);
                    members.add(existingUser);
                } else {
                    /* Add Stranger to Stranger and member list */
                    memberList.updateItem(invitedStranger);
                    strangerList.updateItem(invitedStranger);
                    members.add(invitedStranger);
                }
            }

            putData("group", group);
            putData("members", members);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad invited member json data");

        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get invited member");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");
            ArrayList<User> members = (ArrayList<User>) data.get("members");

            if (group == null || members == null) {
                return;
            }

            Log.v("POKO", "START TO save group member DATA.");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                for (User member : members) {
                    ContentValues values = Serializer.obtainGroupMemberValues(group, member);

                    // Add member data.
                    PokoDatabaseHelper.insertOrIgnoreGroupMemberData(db, values);
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "saved group member successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save group member data.");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
