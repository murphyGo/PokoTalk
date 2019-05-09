package com.murphy.pokotalk.listener.group;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddGroupListener extends PokoServer.PokoListener {
    public AddGroupListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.addGroupName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        GroupList groupList = DataCollection.getInstance().getGroupList();
        try {
            JSONObject jsonObject = data.getJSONObject("group");
            Group group = PokoParser.parseGroup(jsonObject);
            group = groupList.updateItem(group);

            putData("group", group);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("POKO ERROR", "Bad new group json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to get new group");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Group group = (Group) data.get("group");

            if (group == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE group DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Start a transaction
            db.beginTransaction();
            try {
                PokoDatabaseHelper.insertOrUpdateGroupData(db, group);

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE group DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save group data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}
