package com.murphy.pokotalk.listener.group;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
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

public class MembersExitListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.membersExitName;
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
                Log.e("POKO ERROR", "Member cannot exit since there is no such group");
                return;
            }

            UserList userList = group.getMembers();
            ArrayList<User> members = new ArrayList<>();
            for (int i = 0; i < jsonMembers.length(); i++) {
                JSONObject jsonMember = jsonMembers.getJSONObject(i);
                User exitedUser = PokoParser.parseStranger(jsonMember);
                User realUser = userList.removeItemByKey(exitedUser.getUserId());
                if (realUser == null) {
                    Log.e("POKO ERROR", "Member cannot exit since there is no such user");
                } else {
                    members.add(realUser);
                }
            }

            putData("group", group);
            putData("members", members);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad exit member json data");

        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get exit member");
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

            Log.v("POKO", "START TO remove group member DATA.");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                for (User member : members) {
                    String[] selectionArgs = {Integer.toString(group.getGroupId()),
                                                Integer.toString(member.getUserId())};

                    // Removes member data, but messages of removed member still persists.
                    PokoDatabaseHelper.deleteGroupMemberData(db, selectionArgs);
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "removed group member successfully.");
            } catch (Exception e) {
                Log.v("POKO", "Failed to remove group member data.");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
