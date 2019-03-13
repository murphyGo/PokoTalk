package com.murphy.pokotalk.listener.contact;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;

public class GetContactListListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.getContactListName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList list = collection.getContactList();
        PendingContactList invitedContactList = collection.getInvitedContactList();
        PendingContactList invitingContactList = collection.getInvitingContactList();
        StrangerList strangerList = collection.getStrangerList();
        try {
            list.startUpdateList();
            JSONArray contacts = data.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject jsonObject = contacts.getJSONObject(i);
                Contact contact = PokoParser.parseContact(jsonObject);
                list.updateItem(contact);

                /* Remove from other user lists */
                invitedContactList.removeItemByKey(contact.getUserId());
                invitingContactList.removeItemByKey(contact.getUserId());
                strangerList.removeItemByKey(contact.getUserId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad contact json data");
        } catch (ParseException e) {
            Log.e("POKO ERROR", "Failed to parse date string");
        } finally {
            list.endUpdateList();
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get contact list");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            ContactList contactList = DataCollection.getInstance().getContactList();
            Log.v("POKO", "START TO WRITE contact list DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                // Delete all contact data
                PokoDatabaseHelper.deleteAllContactData(db);

                // Insert or update all contact data
                for (Contact contact : contactList.getList()) {
                    ContentValues userValues = Serializer.obtainUserValues(contact);
                    long count = PokoDatabaseHelper.insertOrUpdateUserData(db, contact, userValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update contact data");
                    }

                    ContentValues contactValues = Serializer.obtainContactValues(contact, false);
                    count = PokoDatabaseHelper.insertOrUpdateContactData(db, contactValues);
                    if (count < 0) {
                        Log.e("POKO", "Failed to update contact data");
                    }
                }

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE contact list DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save contact list data");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}
