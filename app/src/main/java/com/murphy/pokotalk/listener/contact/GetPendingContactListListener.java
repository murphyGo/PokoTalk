package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
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
        PendingContactList invitedList = collection.getInvitedContactList();
        PendingContactList invitingList = collection.getInvitingContactList();
        try {
            invitedList.startUpdateList();
            invitingList.startUpdateList();

            JSONArray contacts = data.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject jsonObject = contacts.getJSONObject(i);
                PendingContact contact = PokoParser.parsePendingContact(jsonObject);

                /* Check invited field of pending contact */
                /* 1: I was invited, 0: I invited */
                Boolean invited = PokoParser.parseContactInvitedField(jsonObject);
                if (invited)
                    invitedList.updateItem(contact);
                else
                    invitingList.updateItem(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad pending contact json data");
        } finally {
            invitedList.endUpdateList();
            invitingList.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get pending contact list");
    }
}
