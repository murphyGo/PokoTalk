package com.murphy.pokotalk.listener;

import android.text.TextUtils;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH시", Locale.KOREA);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            /* Set session login and expire date */
            Calendar sessionExpire = Calendar.getInstance();
            sessionExpire.setTimeInMillis(data.getLong("sessionExpire"));
            session.setLogined();
            session.setSessionExpire(sessionExpire);

            /* Parse user data */
            JSONObject userInfo = data.getJSONObject("data");
            String email = userInfo.getString("email");
            String nickname = userInfo.getString("nickname");
            String picture = userInfo.getString("picture");

            /* Parse last seen string to Calendar */
            SimpleDateFormat lastSeenFormat = new SimpleDateFormat(Constants.dateFormat, Locale.KOREA);
            lastSeenFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date lastSeenDate = lastSeenFormat.parse(userInfo.getString("lastSeen"));
            Calendar lastSeen = Calendar.getInstance();
            lastSeen.setTime(lastSeenDate);

            Log.v("EXPIRE", dateFormat.format(sessionExpire.getTime()));
            Log.v("LAST SEEN", dateFormat.format(lastSeen.getTime()));

            /* Create user object and give to session */
            Contact user = new Contact(email, nickname, picture, lastSeen);
            session.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callError(Status status, Object... args) {

    }
}
