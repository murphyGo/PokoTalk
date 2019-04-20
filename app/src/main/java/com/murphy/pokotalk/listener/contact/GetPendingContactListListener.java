package com.murphy.pokotalk.listener.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.json.Serializer;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactPokoList;
import com.murphy.pokotalk.data.user.StrangerPokoList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GetPendingContactListListener extends PokoServer.PokoListener {
    public GetPendingContactListListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.getPendingContactListName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactPokoList contactList = collection.getContactList();
        PendingContactPokoList invitedContactList = collection.getInvitedContactList();
        PendingContactPokoList invitingContactList = collection.getInvitingContactList();
        StrangerPokoList strangerList = collection.getStrangerList();
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

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            DataCollection collection = DataCollection.getInstance();
            PendingContactPokoList invitedList = collection.getInvitedContactList();
            PendingContactPokoList invitingList = collection.getInvitingContactList();

            Log.v("POKO", "START TO WRITE pending contact list DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Delete all pending contact data
                PokoDatabaseHelper.deleteAllPendingContactData(db);

                // Insert all invited pending contact data
                for (PendingContact contact : invitedList.getList()) {
                    ContentValues userValues = Serializer.obtainUserValues(contact);
                    long count = PokoDatabaseHelper.insertOrUpdateUserData(db, contact, userValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update pending contact data");
                    }

                    ContentValues contactValues = Serializer.obtainContactValues(contact, true);
                    count = PokoDatabaseHelper.insertOrUpdateContactData(db, contactValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update pending contact data");
                    }
                }

                // Insert all inviting pending contact data
                for (PendingContact contact : invitingList.getList()) {
                    ContentValues userValues = Serializer.obtainUserValues(contact);
                    long count = PokoDatabaseHelper.insertOrUpdateUserData(db, contact, userValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update pending contact data");
                    }

                    ContentValues contactValues = Serializer.obtainContactValues(contact, false);
                    count = PokoDatabaseHelper.insertOrUpdateContactData(db, contactValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update pending contact data");
                    }
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE pending contact list DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save pending contact list data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
