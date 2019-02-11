package com.murphy.pokotalk.listener.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetGroupListListener extends PokoServer.PokoListener {
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
                Group group = PokoParser.parseGroup(jsonObject);
                list.updateItem(group);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad group json data");
        } finally {
            list.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get group list");
    }
}
