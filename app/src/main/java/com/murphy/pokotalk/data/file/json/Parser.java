package com.murphy.pokotalk.data.file.json;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Parser {
    protected static DataCollection collection = DataCollection.getInstance();

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
        return parseContact(userJson);
    }

    public static Calendar epochInMillsToCalendar(long epochInMills) {
        Calendar result =  Calendar.getInstance();
        result.setTimeInMillis(epochInMills);
        return result;
    }

    public static Stranger parseStranger(JSONObject userJson) throws JSONException {
        Stranger user = new Stranger();
        user.setUserId(userJson.getInt("userId"));
        user.setEmail(userJson.getString("email"));
        user.setNickname(userJson.getString("nickname"));
        user.setPicture(userJson.getString("picture"));

        return user;
    }

    public static PendingContact parsePendingContact(JSONObject userJson) throws JSONException {
        PendingContact user = new PendingContact();
        user.setUserId(userJson.getInt("userId"));
        user.setEmail(userJson.getString("email"));
        user.setNickname(userJson.getString("nickname"));
        user.setPicture(userJson.getString("picture"));

        return user;
    }

    public static Contact parseContact(JSONObject userJson) throws JSONException {
        Contact user = new Contact();
        user.setUserId(userJson.getInt("userId"));
        user.setEmail(userJson.getString("email"));
        user.setNickname(userJson.getString("nickname"));
        user.setPicture(userJson.getString("picture"));
        user.setLastSeen(epochInMillsToCalendar(userJson.getLong("lastSeen")));

        return user;
    }

    public static ContactList.ContactGroupRelation parseContactGroupRelation(JSONObject jsonObject)
        throws JSONException {
        ContactList.ContactGroupRelation relation = new ContactList.ContactGroupRelation();
        relation.setContactUserId(jsonObject.getInt("contactUserId"));
        relation.setGroupId(jsonObject.getInt("groupId"));

        return relation;
    }

    /** NOTE: Before parsing group and message, all the user must be parsed and
     * should be in user list */
    public static Group parseGroup(JSONObject jsonGroup) throws JSONException {
        Group group = new Group();
        int userId = Session.getInstance().getUser().getUserId();
        UserList memberList = group.getMembers();
        JSONArray jsonMembers = jsonGroup.getJSONArray("members");

        group.setGroupId(jsonGroup.getInt("groupId"));
        group.setGroupName(jsonGroup.getString("groupName"));
        try {
            group.setAlias(jsonGroup.getString("alias"));
        } catch (JSONException e) {
            group.setAlias(null);
        }

        group.setNbNewMessages(jsonGroup.getInt("nbNewMessages"));

        /* Parse members */
        for (int i = 0; i < jsonMembers.length(); i++) {
            JSONObject jsonMember = jsonMembers.getJSONObject(i);
            int memberUserId = jsonMember.getInt("userId");
            if (memberUserId == userId) {
                continue;
            }
            User member = collection.getUserById(memberUserId);
            if (member == null) {
                throw new JSONException("Can not find member");
            }
            memberList.updateItem(member);
        }

        return group;
    }

    public static PokoMessage parseMessage(JSONObject userJson) throws JSONException {
        Contact user = Session.getInstance().getUser();
        PokoMessage message = new PokoMessage();
        message.setMessageId(userJson.getInt("messageId"));
        message.setMessageType(userJson.getInt("messageType"));
        message.setDate(epochInMillsToCalendar(userJson.getLong("date")));
        message.setImportanceLevel(userJson.getInt("importanceLevel"));
        message.setContent(userJson.getString("content"));
        message.setNbNotReadUser(userJson.getInt("nbNotReadUser"));
        message.setAcked(userJson.getBoolean("acked"));

        /* Parse writer user */
        JSONObject jsonWriter = userJson.getJSONObject("writer");
        int memberUserId = jsonWriter.getInt("userId");
        User member = collection.getUserById(memberUserId);
        if (member == null) {
            if (user.getUserId() == memberUserId) {
                message.setWriter(user);
            } else {
                throw new JSONException("Can not find writer");
            }
        } else {
            message.setWriter(member);
        }

        return message;
    }
}
