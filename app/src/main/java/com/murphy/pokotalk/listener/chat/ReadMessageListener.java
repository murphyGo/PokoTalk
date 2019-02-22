package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class ReadMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.readMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();
        try {
            JSONArray messages = data.getJSONArray("messages");
            int groupId = data.getInt("groupId");

            /* Find group for the message */
            Group group = groupList.getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Can't read messages since there is no such group.");
                return;
            }

            /* Parse all message and sort in time */
            MessageList messageList = group.getMessageList();
            ArrayList<PokoMessage> readMessages = new ArrayList<>();

            for (int i = 0; i < messages.length(); i++) {
                JSONObject jsonMessage = messages.getJSONObject(i);
                PokoMessage message = PokoParser.parseMessage(jsonMessage);
                messageList.updateItem(message);
                readMessages.add(messageList.getItemByKey(messageList.getKey(message)));
                message = messageList.getItemByKey(messageList.getKey(message));

                /* If the message is history, send get history */
                if (message.getMessageType() == PokoMessage.MEMBER_JOIN) {
                    PokoServer.getInstance(null).sendGetMemberJoinHistory(groupId, message.getMessageId());
                }
            }

            messageList.sortItemsByKey();

            putData("group", group);
            putData("messages", readMessages);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad read message json data");
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to read messages");
    }
}
