package com.murphy.pokotalk.listener.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
}
