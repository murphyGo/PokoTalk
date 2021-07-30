package com.murphy.pokotalk.data.db.json;

import android.database.Cursor;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.schema.EventLocationSchema;
import com.murphy.pokotalk.data.db.schema.EventsSchema;
import com.murphy.pokotalk.data.db.schema.GroupsSchema;
import com.murphy.pokotalk.data.db.schema.MessagesSchema;
import com.murphy.pokotalk.data.db.schema.SessionSchema;
import com.murphy.pokotalk.data.db.schema.UsersSchema;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Parser {
    public static Contact parseUser(Cursor cursor) {
        Contact user = new Contact();
        int pictureIndex = cursor.getColumnIndexOrThrow(SessionSchema.Entry.PICTURE);
        int lastSeenIndex = cursor.getColumnIndexOrThrow(SessionSchema.Entry.LAST_SEEN);

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(SessionSchema.Entry.USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(SessionSchema.Entry.EMAIL)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(SessionSchema.Entry.NICKNAME)));

        if (cursor.isNull(pictureIndex)) {
            user.setPicture(null);
        } else {
            user.setPicture(cursor.getString(pictureIndex));
        }

        if (cursor.isNull(lastSeenIndex)) {
            user.setLastSeen(null);
        } else {
            user.setLastSeen(epochInMillsToCalendar(cursor.getLong(lastSeenIndex)));
        }

        return user;
    }

    public static Calendar epochInMillsToCalendar(long epochInMills) {
        Calendar result =  Calendar.getInstance();
        result.setTimeInMillis(epochInMills);
        result.setTimeZone(Constants.timeZone);
        return result;
    }

    public static Stranger parseStranger(Cursor cursor) {
        Stranger user = new Stranger();

        int pictureIndex = cursor.getColumnIndexOrThrow(UsersSchema.Entry.PICTURE);

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(UsersSchema.Entry.USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.EMAIL)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.NICKNAME)));

        if (cursor.isNull(pictureIndex)) {
            user.setPicture(null);
        } else {
            user.setPicture(cursor.getString(pictureIndex));
        }

        return user;
    }

    public static PendingContact parsePendingContact(Cursor cursor) {
        PendingContact user = new PendingContact();

        int pictureIndex = cursor.getColumnIndexOrThrow(UsersSchema.Entry.PICTURE);

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(UsersSchema.Entry.USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.EMAIL)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.NICKNAME)));

        if (cursor.isNull(pictureIndex)) {
            user.setPicture(null);
        } else {
            user.setPicture(cursor.getString(pictureIndex));
        }

        return user;
    }

    public static Contact parseContact(Cursor cursor) {
        Contact user = new Contact();

        int pictureIndex = cursor.getColumnIndexOrThrow(UsersSchema.Entry.PICTURE);
        int lastSeenIndex = cursor.getColumnIndexOrThrow(UsersSchema.Entry.LAST_SEEN);

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(UsersSchema.Entry.USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.EMAIL)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(UsersSchema.Entry.NICKNAME)));

        if (cursor.isNull(pictureIndex)) {
            user.setPicture(null);
        } else {
            user.setPicture(cursor.getString(pictureIndex));
        }

        if (cursor.isNull(lastSeenIndex)) {
            user.setLastSeen(null);
        } else {
            user.setLastSeen(epochInMillsToCalendar(cursor.getLong(lastSeenIndex)));
        }

        return user;
    }

    public static Group parseGroup(Cursor cursor) {
        Group group = new Group();

        int nameIndex = cursor.getColumnIndexOrThrow(GroupsSchema.Entry.NAME);
        int aliasIndex = cursor.getColumnIndexOrThrow(GroupsSchema.Entry.ALIAS);

        group.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow(GroupsSchema.Entry.GROUP_ID)));
        group.setNbNewMessages(cursor.getInt(cursor.getColumnIndexOrThrow(GroupsSchema.Entry.NB_NEW_MESSAGES)));
        group.setAck(cursor.getInt(cursor.getColumnIndexOrThrow(GroupsSchema.Entry.ACK)));

        if (cursor.isNull(nameIndex)) {
            group.setGroupName(null);
        } else {
            group.setGroupName(cursor.getString(nameIndex));
        }

        if (cursor.isNull(aliasIndex)) {
            group.setAlias(null);
        } else {
            group.setAlias(cursor.getString(aliasIndex));
        }

        return group;
    }

    public static PokoMessage parseMessage(Cursor cursor) throws Exception {
        DataCollection collection = DataCollection.getInstance();
        Contact user = Session.getInstance().getUser();
        PokoMessage message = new PokoMessage();

        // Get indexes
        int contentsIndex = cursor.getColumnIndexOrThrow(MessagesSchema.Entry.CONTENTS);
        int specialContentsIndex = cursor.getColumnIndexOrThrow(MessagesSchema.Entry.SPECIAL_CONTENTS);
        int nbNotReadIndex = cursor.getColumnIndexOrThrow(MessagesSchema.Entry.NB_NOT_READ);
        int importanceIndex = cursor.getColumnIndexOrThrow(MessagesSchema.Entry.IMPORTANCE);

        // Parse data and set to PokoMessage object
        message.setMessageId(cursor.getInt(cursor.getColumnIndexOrThrow(MessagesSchema.Entry.MESSAGE_ID)));
        message.setMessageType(cursor.getInt(cursor.getColumnIndexOrThrow(MessagesSchema.Entry.MESSAGE_TYPE)));
        message.setDate(epochInMillsToCalendar(
                cursor.getLong(cursor.getColumnIndexOrThrow(MessagesSchema.Entry.DATE))));

        if (cursor.isNull(importanceIndex)) {
            message.setImportanceLevel(PokoMessage.IMPORTANCE_NORMAL);
        } else {
            message.setImportanceLevel(cursor.getInt(importanceIndex));
        }
        if (cursor.isNull(contentsIndex)) {
            message.setContent("");
        } else {
            message.setContent(cursor.getString(contentsIndex));
        }
        if (cursor.isNull(specialContentsIndex)) {
            message.setSpecialContent(null);
        } else {
            message.setSpecialContent(cursor.getString(specialContentsIndex));
        }
        if (cursor.isNull(nbNotReadIndex)) {
            message.setNbNotReadUser(0);
        } else {
            message.setNbNotReadUser(cursor.getInt(nbNotReadIndex));
        }

        /* Parse writer user */
        int memberUserId = cursor.getInt(cursor.getColumnIndexOrThrow(MessagesSchema.Entry.USER_ID));
        User member = collection.getUserById(memberUserId);
        if (member == null) {
            if (user.getUserId() == memberUserId) {
                message.setWriter(user);
            } else {
                throw new Exception("Can not find writer with id " + memberUserId);
            }
        } else {
            message.setWriter(member);
        }

        return message;
    }

    public static PokoEvent parseEvent(Cursor cursor) throws Exception {
        DataCollection collection = DataCollection.getInstance();
        EventList eventList = collection.getEventList();
        Contact user = Session.getInstance().getUser();
        PokoEvent event = new PokoEvent();

        int descriptionIndex = cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_DESCRIPTION);
        int groupIdIndex = cursor.getColumnIndexOrThrow(EventsSchema.Entry.GROUP_ID);

        event.setEventId(cursor.getInt(cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_ID)));
        event.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_NAME)));
        event.setAck(cursor.getInt(cursor.getColumnIndexOrThrow(EventsSchema.Entry.ACK)));
        event.setState(cursor.getInt(cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_STARTED)));
        event.setEventDate(
                epochInMillsToCalendar(
                        cursor.getLong(cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_DATE))));

        if (cursor.isNull(descriptionIndex)) {
            event.setDescription(null);
        } else {
            event.setDescription(cursor.getString(descriptionIndex));
        }

        if (cursor.isNull(groupIdIndex)) {
            eventList.removeEventGroupRelationByEventId(event.getEventId());
        } else {
            int groupId = cursor.getInt(groupIdIndex);
            eventList.putEventGroupRelation(event.getEventId(), groupId);
        }

        /* Parse creator user */
        int creatorUserId = cursor.getInt(cursor.getColumnIndexOrThrow(EventsSchema.Entry.EVENT_CREATOR));
        User creator = collection.getUserById(creatorUserId);
        if (creator == null) {
            if (user.getUserId() == creatorUserId) {
                event.setCreator(user);
            } else {
                throw new Exception("Can not find writer");
            }
        } else {
            event.setCreator(creator);
        }

        return event;
    }

    public static EventLocation parseEventLocation(Cursor cursor) {
        EventLocation location = new EventLocation();

        int categoryIndex = cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_CATEGORY);
        int addressIndex = cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_ADDRESS);
        double latitude = cursor.getDouble(
                cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_LATITUDE));
        double longitude = cursor.getDouble(
                cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_LONGITUDE));
        LatLng latLng = new LatLng(latitude, longitude);

        location.setTitle(cursor.getString(
                cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_TITLE)));
        location.setMeetingDate(
                epochInMillsToCalendar(
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(EventLocationSchema.Entry.LOCATION_MEETING_DATE))));
        location.setLatLng(latLng);

        if (cursor.isNull(categoryIndex)) {
            location.setCategory(null);
        } else {
            location.setCategory(cursor.getString(categoryIndex));
        }

        if (cursor.isNull(addressIndex)) {
            location.setAddress(null);
        } else {
            location.setAddress(cursor.getString(addressIndex));
        }

        return location;
    }


    /** Parsing methods using jsonObjects */
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
        DataCollection collection = DataCollection.getInstance();
        Contact user = Session.getInstance().getUser();
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
        group.setAck(jsonGroup.getInt("ack"));

        /* Parse members */
        for (int i = 0; i < jsonMembers.length(); i++) {
            JSONObject jsonMember = jsonMembers.getJSONObject(i);
            int memberUserId = jsonMember.getInt("userId");

            User member = collection.getUserById(memberUserId);
            if (member == null) {
                if (user.getUserId() == memberUserId) {
                    member = user;
                } else {
                    throw new JSONException("Can not find member");
                }
            }
            memberList.updateItem(member);
        }

        return group;
    }

    public static PokoMessage parseMessage(JSONObject userJson) throws JSONException {
        DataCollection collection = DataCollection.getInstance();
        Contact user = Session.getInstance().getUser();
        PokoMessage message = new PokoMessage();
        message.setMessageId(userJson.getInt("messageId"));
        message.setMessageType(userJson.getInt("messageType"));
        message.setDate(epochInMillsToCalendar(userJson.getLong("date")));
        message.setImportanceLevel(userJson.getInt("importanceLevel"));
        try{
            message.setContent(userJson.getString("content"));
        } catch (JSONException e) {
            message.setContent(null);
        }
        try{
            message.setSpecialContent(userJson.getString("specialContent"));
        } catch (JSONException e) {
            message.setSpecialContent(null);
        }
        message.setNbNotReadUser(userJson.getInt("nbNotReadUser"));

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
