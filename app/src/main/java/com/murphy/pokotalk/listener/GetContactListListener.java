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

import java.text.ParseException;

public class GetContactListListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.sessionLoginName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        ContactList list = DataCollection.getInstance().getContactList();
        try {
            JSONArray contacts = data.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject jsonObject = contacts.getJSONObject(i);
                Contact contact = PokoParser.parseContact(jsonObject);
                list.updateContact(contact);
            }
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad contact json data");
        } catch (ParseException e) {
            Log.e("POKO ERROR", "Failed to parse date string");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get contact list");
    }
}
