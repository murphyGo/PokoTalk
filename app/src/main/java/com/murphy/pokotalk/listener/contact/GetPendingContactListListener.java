package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetPendingContactListListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.getPendingContactListName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        PendingContactList invitedContactList = collection.getInvitedContactList();
        PendingContactList invitingContactList = collection.getInvitingContactList();
        StrangerList strangerList = collection.getStrangerList();
        try {
            invitedContactList.startUpdateList();
            invitingContactList.startUpdateList();

            JSONArray contacts = data.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject jsonObject = contacts.getJSONObject(i);
                PendingContact contact = PokoParser.parsePendingContact(jsonObject);

                /* Check invited field of pending contact */
                /* 1: I was invited, 0: I invited */
                Boolean invited = PokoParser.parseContactInvitedField(jsonObject);
                if (invited) {
                    invitedContactList.updateItem(contact);

                    /* Remove from other user lists */
                    contactList.removeItemByKey(contact.getUserId());
                    invitingContactList.removeItemByKey(contact.getUserId());
                    strangerList.removeItemByKey(contact.getUserId());
                } else {
                    invitingContactList.updateItem(contact);

                    /* Remove from other user lists */
                    contactList.removeItemByKey(contact.getUserId());
                    invitedContactList.removeItemByKey(contact.getUserId());
                    strangerList.removeItemByKey(contact.getUserId());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad pending contact json data");
        } finally {
            invitedContactList.endUpdateList();
            invitingContactList.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get pending contact list");
    }
}
