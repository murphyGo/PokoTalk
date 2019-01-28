package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageAckListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.messageAckName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();
        try {
            JSONObject jsonMessage = data.getJSONObject("message");
            int groupId = jsonMessage.getInt("groupId");
            int userId = jsonMessage.getInt("userId"); // don't use
            int ackStartId = jsonMessage.getInt("ackStart");
            int ackEndId = jsonMessage.getInt("ackEnd");

            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't ack message since there is no such group.");
                return;
            }

            MessageList messageList = group.getMessageList();
            messageList.ackMessages(ackStartId, ackEndId);

        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad message ack json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new message");
    }
}
