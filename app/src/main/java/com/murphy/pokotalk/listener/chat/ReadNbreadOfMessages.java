
package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadNbreadOfMessages extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.readNbreadOfMessages;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();
        try {
            JSONArray jsonNbreads = data.getJSONArray("messages");
            int groupId = data.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read nbreads since there is no such group.");
                return;
            }

            /* Parse all message and sort in time */
            MessageList messageList = group.getMessageList();

            for (int i = 0; i < jsonNbreads.length(); i++) {
                JSONObject jsonNbread = jsonNbreads.getJSONObject(i);
                int messageId = jsonNbread.getInt("messageId");
                int nbread = jsonNbread.getInt("nbread");
                if (nbread < 0) {
                    continue;
                }

                PokoMessage message = messageList.getItemByKey(messageId);
                if (message != null) {
                    message.setNbNotReadUser(nbread);
                }
            }

            putData("group", group);
            putData("nbreads", jsonNbreads);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad read nbread of message json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to read nbread of messages");
    }
}
