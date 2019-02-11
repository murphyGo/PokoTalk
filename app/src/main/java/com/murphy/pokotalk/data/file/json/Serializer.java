package com.murphy.pokotalk.data.file.json;

import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.Contact;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Serializer {

    public static JSONObject makeSessionJSON(Session session) throws JSONException {
        JSONObject jsonSession = new JSONObject();
        jsonSession.put("sessionId", session.getSessionId());
        jsonSession.put("sessionExpire", calendarToEpochInMills(session.getSessionExpire()));
        JSONObject jsonUser = makeUserJSON(session.getUser());
        jsonSession.put("user", jsonUser);

        return jsonSession;
    }

    public static JSONObject makeUserJSON(Contact user) throws JSONException {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("userId", user.getUserId());
        jsonUser.put("email", user.getEmail());
        jsonUser.put("nickname", user.getNickname());
        jsonUser.put("picture", user.getPicture());
        jsonUser.put("lastSeen", calendarToEpochInMills(user.getLastSeen()));

        return jsonUser;
    }

    public static long calendarToEpochInMills(Calendar calendar) {
        return calendar.getTimeInMillis();
    }
}
