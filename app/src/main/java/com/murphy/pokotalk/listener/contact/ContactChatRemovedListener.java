package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactChatRemovedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.contactChatRemovedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        try {
            int contactId = data.getInt("contactId");
            Contact contact = contactList.getItemByKey(contactId);

            if (contact == null) {
                Log.e("POKO ERROR", "Cannot remove contact chat since no such contact");
                return;
            }

            contact.setGroupId(null);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to remove contact chat");
    }
}
