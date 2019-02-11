package com.murphy.pokotalk.listener.chat;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;

public class SendMessageListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.sendMessageName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        try {
            int groupId = data.getInt("groupId");
            int messageId = data.getInt("messageId");
            int sendId = data.getInt("sendId");
            int nbread = data.getInt("nbread");
            Calendar date = PokoParser.parseDateString(data.getString("date"));

            Group group = collection.getGroupList().getItemByKey(groupId);
            if (group == null) {
                Log.e("POKO ERROR", "Cannot send message since no such contact");
                return;
            }

            MessageList messageList = group.getMessageList();
            if (!messageList.moveSentMessageToMessageList(
                    sendId, messageId, nbread, date)) {
                Log.e("POKO ERROR", "Failed to send message to message list");
                return;
            }

            putData("group", group);
            putData("message", messageList.getItemByKey(messageId));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad send message json data");
        } catch (ParseException e){
            Log.e("POKO ERROR", "Failed to parse date");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to send message");
    }
}
