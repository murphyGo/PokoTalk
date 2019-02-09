package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.StrangerList;
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
        ContactList contactList = collection.getContactList();
        PendingContactList invitedContactList = collection.getInvitedContactList();
        PendingContactList invitingContactList = collection.getInvitingContactList();
        StrangerList strangerList = collection.getStrangerList();
        try {
            JSONObject jsonObject = data.getJSONObject("contact");
            Contact contact = PokoParser.parseContact(jsonObject);
            contactList.updateItem(contact);

            /* Remove from other user lists */
            invitedContactList.removeItemByKey(contact.getUserId());
            invitingContactList.removeItemByKey(contact.getUserId());
            strangerList.removeItemByKey(contact.getUserId());

            putData("contact", contactList.getItemByKey(contact.getUserId()));
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
