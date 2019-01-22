package com.murphy.pokotalk.listener;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.ContactList;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactRemovedListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.contactRemovedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        try {
            JSONObject jsonObject = data.getJSONObject("contact");
            int userId = jsonObject.getInt("userId");

            contactList.removeContactById(userId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get removed contact data");
    }
}
