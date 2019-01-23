package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class NewContactListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.newContactName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        PendingContactList invitedList = collection.getInvitedContactList();
        PendingContactList invitingList = collection.getInvitingContactList();
        ContactList contactList = collection.getContactList();
        try {
            JSONObject jsonObject = data.getJSONObject("contact");
            Contact contact = PokoParser.parseContact(jsonObject);

            invitedList.removeItemByKey(contact.getUserId());
            invitingList.removeItemByKey(contact.getUserId());
            contactList.updateItem(contact);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new contact json data");
        } catch (ParseException e) {
            Log.e("POKO ERROR", "Bad lastSeen data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new contact data");
    }
}
