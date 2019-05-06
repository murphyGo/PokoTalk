package com.murphy.pokotalk.listener.contact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ContactChatRemovedListener extends PokoServer.PokoListener {
    public ContactChatRemovedListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.contactChatRemovedName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        GroupPokoList groupList = collection.getGroupList();

        try {
            int userId = data.getInt("userId");
            int groupId = data.getInt("groupId");

            Contact contact = contactList.getItemByKey(userId);
            Group group = groupList.getItemByKey(groupId);

            if (contact == null || group == null) {
                Log.e("POKO ERROR", "Cannot remove contact chat since no such contact or group");
                return;
            }

            ContactList.ContactGroupRelation relation =
                    contactList.removeContactGroupRelationByUserId(userId);
            if (relation == null) {
                Log.e("POKO ERROR", "Failed to remove contact group relation");
            }

            putData("contact", contact);
            putData("group", group);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to remove contact chat");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Contact contact = (Contact) data.get("contact");
            Group group = (Group) data.get("group");

            if (contact == null || group == null) {
                return;
            }

            Log.v("POKO", "START TO remove contact chat");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            db.beginTransaction();
            try {
                // make contact chat data with same group null
                String[] selectionArgs = {Integer.toString(group.getGroupId())};
                PokoDatabaseHelper.updateContactChatDataNull(db, selectionArgs);

                Log.v("POKO", "remove contact chat successfully");
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.v("POKO", "Failed to remove contact chat");
            } finally {
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
