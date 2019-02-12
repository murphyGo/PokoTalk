package com.murphy.pokotalk.listener.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class ExitGroupListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.exitGroupName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        try {
            JSONObject data = (JSONObject) args[0];
            DataCollection collection = DataCollection.getInstance();
            GroupList groupList = collection.getGroupList();
            int groupId = data.getInt("groupId");

            if (groupList.removeItemByKey(groupId) == null) {
                Log.e("POKO ERROR", "No such group to remove with this group id");
            }

            putData("groupId", groupId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad group exit json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to exit group");
    }
}
