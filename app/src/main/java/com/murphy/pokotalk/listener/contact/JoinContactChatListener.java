package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinContactChatListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.joinContactChatName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        GroupList groupList = collection.getGroupList();
        try {
            JSONObject jsonObject = data.getJSONObject("group");
            int userId = data.getInt("userId");
            Contact contact = contactList.getItemByKey(userId);

            if (contact == null) {
                Log.e("POKO ERROR", "Cannot add contact chat since no such contact");
                return;
            }

            Group group = PokoParser.parseGroup(jsonObject);
            group.setContact(contact);
            groupList.updateItem(group);
            contact.setGroupId(group.getGroupId());
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to join contact chat");
    }
}
