package com.murphy.pokotalk.listener.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class AddGroupListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.addGroupName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        GroupList groupList = DataCollection.getInstance().getGroupList();
        try {
            JSONObject jsonObject = data.getJSONObject("group");
            Group group = PokoParser.parseGroup(jsonObject);
            groupList.updateItem(group);

            putData("group", groupList.getItemByKey(group.getGroupId()));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad new group json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new group");
    }
}
