package com.murphy.pokotalk.listener.session;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.file.PokoDatabaseHelper;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.file.schema.SessionSchema;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.server.parser.PokoParser;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class SessionLoginListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.sessionLoginName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        JSONObject data = (JSONObject) args[0];
        Session session = Session.getInstance();
        try {
            String sessionId = data.getString("sessionId");
            if (!TextUtils.equals(sessionId, session.getSessionId())) {
                return;
            }

            /* Expire date is long type epoch time, last seen is string to be parsed */
            /* Set session login and expire date */
            Calendar sessionExpire = Calendar.getInstance();
            long sessionExpireLong = data.getLong("sessionExpire");
            sessionExpire.setTimeInMillis(sessionExpireLong);
            session.setSessionExpire(sessionExpire);

            /* Parse user data */
            JSONObject userInfo = data.getJSONObject("user");
            int userId = userInfo.getInt("userId");
            String email = userInfo.getString("email");
            String nickname = userInfo.getString("nickname");
            String picture = userInfo.getString("picture");

            /* Parse last seen string to Calendar */
            Calendar lastSeen = Parser.epochInMillsToCalendar(userInfo.getLong("lastSeen"));

            Log.v("USER DATA", userInfo.toString());
            Log.v("EXPIRE", PokoParser.formatCalendar(sessionExpire));
            Log.v("LAST SEEN", PokoParser.formatCalendar(lastSeen));

            /* Create user object and give to session */
            Contact user = new Contact(userId, email, nickname, picture, lastSeen);
            Contact oldUser = session.getUser();
            /* Update if user exists, otherwise set as a user */
            if (oldUser != null) {
                oldUser.update(user);
            } else {
                session.setUser(user);
            }

            /* Request contact list and group list of the user */
            PokoServer server = PokoServer.getInstance(null);
            server.sendGetContactList();
            server.sendGetGroupList();

            putData("session", session);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            /* Login user */
            if (!session.hasLogined()) {
                session.setLogined();
            }
        }
    }

    @Override
    public void callError(Status status, Object... args) {

    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return new DatabaseJob();
    }

    static class DatabaseJob extends PokoAsyncDatabaseJob {
        @Override
        protected void doJob(HashMap<String, Object> data) {
            Session session = (Session) data.get("session");
            User user = session.getUser();

            if (session == null || user == null) {
                return;
            }

            Log.v("POKO", "START TO WRITE SESSION DATA " + user.getNickname() + ", " + user.getUserId());

            /* Save session data */
            SQLiteDatabase db = getWritableDatabase();

            ContentValues sessionValues = Serializer.obtainSessionValues(session);
            ContentValues userValues = Serializer.obtainUserValues(user);

            // Start a transaction
            db.beginTransaction();
            try {
                // Remove contact data of the user
                String[] selectionArgs = {Integer.toString(user.getUserId())};
                PokoDatabaseHelper.deleteContactData(db, selectionArgs);

                // Delete any other session data
                db.delete(SessionSchema.Entry.TABLE_NAME, null, null);

                // Insert or update user data
                PokoDatabaseHelper.insertOrUpdateUserData(db, user, userValues);

                // Insert session data
                db.insertWithOnConflict(SessionSchema.Entry.TABLE_NAME,
                        null, sessionValues, SQLiteDatabase.CONFLICT_REPLACE);

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE SESSION DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save session data");
            } finally {
                // End a transaction
                db.endTransaction();
            }
        }
    }
}