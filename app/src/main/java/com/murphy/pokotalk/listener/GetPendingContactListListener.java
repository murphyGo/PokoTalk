package com.murphy.pokotalk.listener;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.data.ContactList;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.parser.PokoParser;
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
        ContactList invitedList = collection.getInvitedContactList();
        ContactList invitingList = collection.getInvitingContactList();
        try {
            JSONArray contacts = data.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject jsonObject = contacts.getJSONObject(i);
                Contact contact = PokoParser.parsePendingContact(jsonObject);

                /* Check invited field of pending contact */
                /* 1: I was invited, 0: I invited */
                Boolean invited = jsonObject.getBoolean("invited");
                if (invited)
                    invitedList.updateContact(contact);
                else
                    invitingList.updateContact(contact);
            }
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad pending contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get pending contact list");
    }
}
