package com.murphy.pokotalk.data.file.json;

import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.Contact;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Parser {
    public static Session parseSessionJSON(JSONObject jsonSession) throws JSONException {
        Session session = Session.getInstance();
        String sessionId = jsonSession.getString("sessionId");
        Calendar sessionExpire = epochInMillsToCalendar(jsonSession.getLong("sessionExpire"));
        Contact user = parseUserJSON(jsonSession.getJSONObject("user"));

        session.setSessionId(sessionId);
        session.setSessionExpire(sessionExpire);
        session.setUser(user);

        return session;
    }

    public static Contact parseUserJSON(JSONObject userJson) throws JSONException {
        Contact user = new Contact();
        user.setUserId(userJson.getInt("userId"));
        user.setEmail(userJson.getString("email"));
        user.setNickname(userJson.getString("nickname"));
        user.setPicture(userJson.getString("picture"));
        user.setLastSeen(epochInMillsToCalendar(userJson.getLong("lastSeen")));

        return user;
    }

    public static Calendar epochInMillsToCalendar(long epochInMills) {
        Calendar result =  Calendar.getInstance();
        result.setTimeInMillis(epochInMills);
        return result;
    }
}
