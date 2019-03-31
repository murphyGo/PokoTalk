package com.murphy.pokotalk.listener.group;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ExitGroupListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.exitGroupName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        try {
            JSONObject data = (JSONObject) args[0];
            DataCollection collection = DataCollection.getInstance();
            GroupPokoList groupList = collection.getGroupList();
            int groupId = data.getInt("groupId");

            if (groupList.removeItemByKey(groupId) == null) {
                Log.e("POKO ERROR", "No such group to remove with this group id");
            }

            putData("groupId", groupId);
        } catch (JSONException e) {
            Log.e("POKO ERROR", "Bad group exit json data");
        }
    }

    @Override
    public void callError(Status status, Object... args) {
        Log.e("POKO ERROR", "Failed to exit group");
    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Integer groupId = (Integer) data.get("groupId");

            if (groupId == null) {
                return;
            }

            Log.v("POKO", "START TO remove group DATA");

            /* Get database to write */
            SQLiteDatabase db = getWritableDatabase();

            // Set foreign key constraint enabled so that members and messages are also deleted.
            db.setForeignKeyConstraintsEnabled(true);

            // Start a transaction
            db.beginTransaction();
            try {
                String[] selectionArgs = {Integer.toString(groupId)};

                // Remove group data, all members and messages are removed in cascading way.
                PokoDatabaseHelper.deleteGroupData(db, selectionArgs);

                db.setTransactionSuccessful();
                Log.v("POKO", "remove group DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to remove group data");
            } finally {
                // End a transaction
                db.endTransaction();

                // Set foreign key constraint disabled.
                db.setForeignKeyConstraintsEnabled(false);
            }
        }
    }
}
