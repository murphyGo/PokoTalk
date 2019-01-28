package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class NewMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.newMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();
        try {
            JSONObject jsonMessage = data.getJSONObject("message");
            int groupId = jsonMessage.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read message since there is no such group.");
                return;
            }

            /* Parse message and add sorted by time */
            MessageList messageList = group.getMessageList();
            Message message = PokoParser.parseMessage(jsonMessage);
            messageList.addMessageSortedByTime(message);

        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new message json data");
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new message");
    }
}
