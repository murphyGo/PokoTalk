package com.murphy.pokotalk.listener.session;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.db.json.Serializer;
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

            /* Get up-to-date contact and group and event list */
            PokoServer server = PokoServer.getInstance();
            server.sendGetContactList();
            server.sendGetGroupList();
            server.sendGetEventList();

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
            SQLiteDatabase db = getWritableSessionDatabase();

            ContentValues sessionValues = Serializer.obtainSessionValues(session);

            // Start a transaction
            db.beginTransaction();
            try {
                // Delete any other session data
                PokoDatabaseHelper.deleteAllSessionData(db);

                // Insert session data
                PokoDatabaseHelper.insertOrUpdateSessionData(db, sessionValues);

                db.setTransactionSuccessful();
                Log.v("POKO", "WRITE SESSION DATA successfully");
            } catch (Exception e) {
                Log.v("POKO", "Failed to save session data");
            } finally {
                // End a transaction
                db.endTransaction();

                db.releaseReference();
            }
        }
    }
}