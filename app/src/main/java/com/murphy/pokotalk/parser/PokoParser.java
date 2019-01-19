package com.murphy.pokotalk.parser;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.data.Event;
import com.murphy.pokotalk.data.Group;
import com.murphy.pokotalk.data.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/* Parse JSONObject and String data from PokoTalk server */
public class PokoParser {
    public static Contact parseContact(JSONObject jsonObject) throws JSONException, ParseException {
        Contact result = new Contact();

        result.setUserId(jsonObject.getInt("userId"));
        result.setEmail(jsonObject.getString("email"));
        result.setNickname(jsonObject.getString("nickname"));
        result.setPicture(jsonObject.getString("picture"));
        result.setContactId(jsonObject.getInt("contactId"));
        result.setGroupId(jsonObject.getInt("groupId"));
        result.setLastSeen(parseDateString(jsonObject.getString("lastSeen")));

        return result;
    }

    public static Group parseGroup(JSONObject jsonObject) throws JSONException {

        return null;
    }

    public static Message parseMessage(JSONObject jsonObject) throws JSONException {

        return null;
    }

    public static Event parseEvent(JSONObject jsonObject) throws JSONException {

        return null;
    }

    public static Calendar parseDateString(String dateStr) throws ParseException {
        SimpleDateFormat lastSeenFormat = new SimpleDateFormat(Constants.dateFormat, Locale.KOREA);
        lastSeenFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date lastSeenDate = lastSeenFormat.parse(dateStr);
        Calendar result = Calendar.getInstance();
        result.setTime(lastSeenDate);

        return result;
    }

    public static String formatCalendar(Calendar date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH시", Locale.KOREA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return dateFormat.format(date.getTime());
    }
}
