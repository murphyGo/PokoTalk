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
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewContactListener extends PokoServer.PokoListener {
    public NewContactListener(Context context) {
        super(context);
    }

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
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new contact data");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Contact contact = (Contact) data.get("contact");

            if (contact == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE new contact DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Insert or update new user data
                ContentValues userValues = Serializer.obtainUserValues(contact);
                long count = PokoDatabaseHelper.insertOrUpdateUserData(db, userValues);
                if (count < 0) {
                    Log.e("POKO", "Failed to update new contact data");
                }

                // Insert or update new contact data
                ContentValues contactValues = Serializer.obtainContactValues(contact, false);
                count = PokoDatabaseHelper.insertOrUpdateContactData(db, contactValues);
                if (count < 0) {
                    Log.e("POKO", "Failed to update new contact data");
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE new contact DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save new contact data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
