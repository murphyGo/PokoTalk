package com.murphy.pokotalk.listener.session;

import android.text.TextUtils;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.file.FileManager;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.parser.PokoParser;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONObject;

import java.util.Calendar;

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
            String loginedSessionId = data.getString("sessionId");
            if (!TextUtils.equals(loginedSessionId, session.getSessionId())) {
                return;
            }

            /* Expire date is long type epoch time, last seen is string to be parsed */
            /* Set session login and expire date */
            Calendar sessionExpire = Calendar.getInstance();
            sessionExpire.setTimeInMillis(data.getLong("sessionExpire"));
            session.setSessionExpire(sessionExpire);

            /* Parse user data */
            JSONObject userInfo = data.getJSONObject("user");
            int userId = userInfo.getInt("userId");
            String email = userInfo.getString("email");
            String nickname = userInfo.getString("nickname");
            String picture = userInfo.getString("picture");

            /* Parse last seen string to Calendar */
            Calendar lastSeen = PokoParser.parseDateString(userInfo.getString("lastSeen"));

            Log.v("USER DATA", userInfo.toString());
            Log.v("EXPIRE", PokoParser.formatCalendar(sessionExpire));
            Log.v("LAST SEEN", PokoParser.formatCalendar(lastSeen));

            /* Create user object and give to session */
            Contact user = new Contact(userId, email, nickname, picture, lastSeen);
            session.setUser(user);

            /* Save session data */
            FileManager fileManager = FileManager.getInstance();
            fileManager.saveSession();
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
}
