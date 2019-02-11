package com.murphy.pokotalk.server.parser;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.event.Event;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

import org.json.JSONArray;
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

        Log.v("efw", jsonObject.toString());
        result.setUserId(jsonObject.getInt("userId"));
        result.setEmail(jsonObject.getString("email"));
        result.setNickname(jsonObject.getString("nickname"));
        result.setPicture(jsonObject.getString("picture"));
        if (jsonObject.has("groupId") && !jsonObject.isNull("groupId"))
            result.setGroupId(jsonObject.getInt("groupId"));
        else
            result.setGroupId(null);
        if (jsonObject.has("lastSeen") && !jsonObject.isNull("lastSeen"))
            result.setLastSeen(parseDateString(jsonObject.getString("lastSeen")));
        else
            result.setLastSeen(null);

        return result;
    }

    public static PendingContact parsePendingContact(JSONObject jsonObject) throws JSONException {
        PendingContact result = new PendingContact();

        result.setUserId(jsonObject.getInt("userId"));
        result.setEmail(jsonObject.getString("email"));
        result.setNickname(jsonObject.getString("nickname"));
        result.setPicture(jsonObject.getString("picture"));

        return result;
    }


    public static Stranger parseStranger(JSONObject jsonObject) throws JSONException {
        Stranger result = new Stranger();

        result.setUserId(jsonObject.getInt("userId"));
        result.setEmail(jsonObject.getString("email"));
        result.setNickname(jsonObject.getString("nickname"));
        result.setPicture(jsonObject.getString("picture"));

        return result;
    }

    public static Boolean parseContactInvitedField(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt("invited") != 0 ? true : false;
    }

    /** Parse group
     * parses fields groupId, name, alias, nbNewMessages, members
     * @param jsonObject
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    public static Group parseGroup(JSONObject jsonObject) throws JSONException {
        Group result = new Group();
        DataCollection collection = DataCollection.getInstance();

        result.setGroupId(jsonObject.getInt("groupId"));
        result.setGroupName(jsonObject.getString("name"));
        if (jsonObject.has("nbNewMessages") && !jsonObject.isNull("nbNewMessages"))
            result.setNbNewMessages(jsonObject.getInt("nbNewMessages"));
        if (jsonObject.has("alias") && !jsonObject.isNull("alias"))
            result.setAlias(jsonObject.getString("alias"));
        if (jsonObject.has("members") && !jsonObject.isNull("members")) {
            JSONArray jsonMembers = jsonObject.getJSONArray("members");

            UserList members = result.getMembers();
            for (int i = 0; i < jsonMembers.length(); i++) {
                JSONObject jsonMember = jsonMembers.getJSONObject(i);

                /* Get existing user or create new user */
                User member = parseStranger(jsonMember);
                User exist = collection.getUserById(member.getUserId());
                if (exist == null) {
                    collection.updateUserList(member);
                } else {
                    member = exist;
                }

                /* Add to member */
                members.updateItem(member);
            }
        }

        return result;
    }

    public static Message parseMessage(JSONObject jsonObject) throws JSONException, ParseException {
        Message result = new Message();
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        PendingContactList invitedList = collection.getInvitedContactList();
        PendingContactList invitingList = collection.getInvitingContactList();
        StrangerList strangerList = collection.getStrangerList();

        result.setMessageId(jsonObject.getInt("messageId"));
        result.setContent(jsonObject.getString("content"));
        result.setImportanceLevel(
                parseMessageImportanceLevel(jsonObject.getInt("importance")));
        result.setNbNotReadUser(jsonObject.getInt("nbread"));
        result.setDate(parseDateString(jsonObject.getString("date")));
        result.setMessageType(Message.MESSAGE);
        int userId = jsonObject.getInt("userId");
        User writer;

        User user1 = contactList.getItemByKey(userId);
        User user2 = invitedList.getItemByKey(userId);
        User user3 = invitingList.getItemByKey(userId);
        User user4 = strangerList.getItemByKey(userId);

        if (user1 != null) {
            writer = user1;
        } else if (user2 != null) {
            writer = user2;
        } else if (user3 != null) {
            writer = user3;
        } else if (user4 != null) {
            writer = user4;
        } else {
            /* If writer is unknown, create temporary unknown user */
            Stranger stranger = new Stranger();
            stranger.setUserId(userId);
            stranger.setNickname(Constants.unknownUserNickname);
            stranger.setEmail(Constants.unknownUserEmail);
            strangerList.updateItem(stranger);
            writer = stranger;
        }

        result.setWriter(writer);

        return result;
    }

    public static int parseMessageImportanceLevel(int level) throws ParseException {
        switch (level) {
            case 0:
                return Message.NORMAL;
            case 1:
                return Message.IMPORTANT;
            case 2:
                return Message.VERY_IMPORTANT;
            default:
                throw new ParseException("Unknown importance level", 1);
        }
    }

    public static Event parseEvent(JSONObject jsonObject) throws JSONException {

        return null;
    }

    public static Calendar parseDateString(String dateStr) throws ParseException {
        SimpleDateFormat lastSeenFormat = new SimpleDateFormat(Constants.serverDateFormat, Locale.KOREA);
        lastSeenFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date lastSeenDate = lastSeenFormat.parse(dateStr);
        Calendar result = Calendar.getInstance();
        result.setTime(lastSeenDate);

        return result;
    }

    public static String formatCalendar(Calendar date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return dateFormat.format(date.getTime());
    }
}