package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class AckMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.ackMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        DataCollection collection = DataCollection.getInstance();
        try {
            JSONObject jsonObject = (JSONObject) args[0];
            int groupId = jsonObject.getInt("groupId");
            int toId = jsonObject.getInt("ackEnd");

            Group group = collection.getGroupList().getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "can't ack message since there's no such group.");
                return;
            }

            group.setAck(Math.max(group.getAck(), toId));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "bad ack message json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to ack message");
    }
}
