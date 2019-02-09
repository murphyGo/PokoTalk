package com.murphy.pokotalk.listener.contact;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactDeniedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.contactDeniedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        try {
            JSONObject jsonObject = data.getJSONObject("contact");
            PendingContact contact = PokoParser.parsePendingContact(jsonObject);
            /* The user becomes now stranger */
            collection.moveUserToStrangerList(contact.getUserId());

            putData("userId", contact.getUserId());
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad new contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get contact denied data");
    }
}
