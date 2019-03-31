package com.murphy.pokotalk.listener.contact;

import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JoinContactChatListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.joinContactChatName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        DataCollection collection = DataCollection.getInstance();
        ContactPokoList contactList = collection.getContactList();
        GroupPokoList groupList = collection.getGroupList();
        try {
            JSONObject jsonObject = data.getJSONObject("group");
            int userId = data.getInt("userId");
            Contact contact = contactList.getItemByKey(userId);

            if (contact == null) {
                Log.e("POKO ERROR", "Cannot add contact chat since no such contact");
                return;
            }

            Group group = PokoParser.parseGroup(jsonObject);
            groupList.updateItem(group);

            contactList.putContactGroupRelation(contact.getUserId(), group.getGroupId());

            putData("contact", contact);
            putData("group", groupList.getItemByKey(groupList.getKey(group)));
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad removed contact json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to join contact chat");
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

            Log.v("POKO", "START TO write contact chat");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            db.beginTransaction();
            try {
                // make contact chat data with same group null
                String[] selectionArgs = {Integer.toString(group.getGroupId())};
                PokoDatabaseHelper.updateContactChatDataNull(db, selectionArgs);

                // update contact chat data
                String[] selectionArgs2 = {Integer.toString(contact.getUserId())};
                long count = PokoDatabaseHelper.updateContactChatData(db, group, selectionArgs2);
                if (count <= 0) {
                    throw new SQLiteAbortException("Failed to update contact chat data");
                }

                Log.v("POKO", "write contact chat successfully");
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.v("POKO", "Failed to write contact chat");
            } finally {
                db.endTransaction();
            }
        }
    }
}
