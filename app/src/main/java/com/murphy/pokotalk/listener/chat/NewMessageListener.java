package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.server.parser.PokoParser;
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

            /* Parse message and add sorted by message id */
            MessageList messageList = group.getMessageList();
            Message message = PokoParser.parseMessage(jsonMessage);
            messageList.addMessageSortedById(message);

            /* Add NbNewMessage number */
            collection.acquireGroupSemaphore();
            /* Increment new message number only when the user is not chatting for this group */
            if (collection.getChattingGroup() != group) {
                group.setNbNewMessages(group.getNbNewMessages() + 1);
            }
            collection.releaseGroupSemaphore();

            putData("group", group);
            putData("message", messageList.getItemByKey(message.getMessageId()));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new message json data");
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
        } catch (InterruptedException e) {
            Log.e("POKO ERROR", "Failed to add new message number");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new message");
    }
}
