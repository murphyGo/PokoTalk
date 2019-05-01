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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewPendingContactListener extends PokoServer.PokoListener {
    public NewPendingContactListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.newPendingContactName;
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
            JSONObject jsonObject = data.getJSONObject("contact");
            PendingContact contact = PokoParser.parsePendingContact(jsonObject);

            /* Check invited field of pending contact */
            /* 1: I was invited, 0: I invited */
            Boolean invited = PokoParser.parseContactInvitedField(jsonObject);
            if (invited) {
                invitedContactList.updateItem(contact);
                contact = invitedContactList.getItemByKey(contact.getUserId());

                /* Remove from other user lists */
                contactList.removeItemByKey(contact.getUserId());
                invitingContactList.removeItemByKey(contact.getUserId());
                strangerList.removeItemByKey(contact.getUserId());
            } else {
                invitingContactList.updateItem(contact);
                contact = invitingContactList.getItemByKey(contact.getUserId());

                /* Remove from other user lists */
                contactList.removeItemByKey(contact.getUserId());
                invitedContactList.removeItemByKey(contact.getUserId());
                strangerList.removeItemByKey(contact.getUserId());
            }

            putData("invited", invited);
            putData("pendingContact", contact);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad pending contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get pending contact data");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            PendingContact pendingContact = (PendingContact) data.get("pendingContact");
            Boolean invited = (Boolean) data.get("invited");

            if (pendingContact == null || invited == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE new pending contact DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Insert or update pending contact data
                ContentValues userValues = Serializer.obtainUserValues(pendingContact);
                long count = PokoDatabaseHelper.insertOrUpdateUserData(db, userValues);
                if (count < 0) {
                    Log.e("POKO", "Failed to update pending contact data");
                }

                ContentValues contactValues = Serializer.obtainContactValues(pendingContact, invited);
                count = PokoDatabaseHelper.insertOrUpdateContactData(db, contactValues);
                if (count < 0) {
                    Log.e("POKO", "Failed to update pending contact data");
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE new pending contact DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save new pending contact data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
