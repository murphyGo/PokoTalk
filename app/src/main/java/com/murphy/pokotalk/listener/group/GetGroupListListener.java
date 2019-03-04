package com.murphy.pokotalk.listener.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

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
                /* Parse group */
                Group group = PokoParser.parseGroup(jsonObject);
                list.updateItem(group);

                /* Parse last message */
                if (jsonObject.has("lastMessage")) {
                    JSONObject jsonLastMessage = jsonObject.getJSONObject("lastMessage");
                    PokoMessage lastMessage = PokoParser.parseMessage(jsonLastMessage);
                    group = list.getItemByKey(group.getGroupId());
                    MessageList messageList = group.getMessageList();
                    messageList.updateItem(lastMessage);

                    /* If the message is history, send get history */
                    if (lastMessage.getMessageType() == PokoMessage.MEMBER_JOIN) {
                        PokoServer.getInstance(null).sendGetMemberJoinHistory(group.getGroupId(),
                                lastMessage.getMessageId());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad group json data");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad last message data");
        } finally {
            list.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get group list");
    }
}
