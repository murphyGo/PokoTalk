package com.murphy.pokotalk.server.parser;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactPokoList;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerPokoList;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserPokoList;
import com.naver.maps.geometry.LatLng;

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
    public static final int SERVER_EVENT_ACK_NOT_SEEN = 0;
    public static final int SERVER_EVENT_ACK_SEEN = 1;
    public static final int SERVER_EVENT_ACK_SEEN_STARTED = 2;

    public static Contact parseContact(JSONObject jsonObject) throws JSONException {
        DataCollection collection = DataCollection.getInstance();
        Contact result = new Contact();
        ContactPokoList contactList = collection.getContactList();

        result.setUserId(jsonObject.getInt("userId"));
        result.setEmail(jsonObject.getString("email"));
        result.setNickname(jsonObject.getString("nickname"));
        result.setPicture(jsonObject.getString("picture"));
        if (jsonObject.has("groupId") && !jsonObject.isNull("groupId")) {
            contactList.putContactGroupRelation(result.getUserId(), jsonObject.getInt("groupId"));
        } else {
            if (contactList.getContactGroupRelationByUserId(result.getUserId()) != null) {
                contactList.removeContactGroupRelationByUserId(result.getUserId());
            }
        }
        if (jsonObject.has("lastSeen") && !jsonObject.isNull("lastSeen"))
            result.setLastSeen(Parser.epochInMillsToCalendar(jsonObject.getLong("lastSeen")));
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
        return jsonObject.getInt("invited") != 0;
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

            UserPokoList members = result.getMembers();
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

    public static PokoMessage parseMessage(JSONObject jsonObject) throws JSONException, ParseException {
        // Get lists
        PokoMessage result = new PokoMessage();
        DataCollection collection = DataCollection.getInstance();
        ContactPokoList contactList = collection.getContactList();
        PendingContactPokoList invitedList = collection.getInvitedContactList();
        PendingContactPokoList invitingList = collection.getInvitingContactList();
        StrangerPokoList strangerList = collection.getStrangerList();

        result.setMessageId(jsonObject.getInt("messageId"));
        result.setImportanceLevel(
                parseMessageImportanceLevel(jsonObject.getInt("importance")));
        result.setNbNotReadUser(jsonObject.getInt("nbread"));
        result.setDate(Parser.epochInMillsToCalendar(jsonObject.getLong("date")));
        int userId = jsonObject.getInt("userId");
        User writer;

        User user1 = contactList.getItemByKey(userId);
        User user2 = invitedList.getItemByKey(userId);
        User user3 = invitingList.getItemByKey(userId);
        User user4 = strangerList.getItemByKey(userId);
        User user = Session.getInstance().getUser();

        // Check if it is my message or other user's message
        if (user != null && user.getUserId() == userId) {
            writer = user;
        } else if (user1 != null) {
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

        // Set writer
        result.setWriter(writer);

        // Parse message content
        parseMessageContent(result, jsonObject);

        return result;
    }

    /** parse message content from jsonObject and update given message
     * precondition: writer of message must be set before calling this method */
    public static PokoMessage parseMessageContent(PokoMessage message, JSONObject jsonObject)
            throws JSONException {
        message.setMessageType(jsonObject.getInt("messageType"));

        switch (message.getMessageType()) {
            case PokoMessage.TEXT_MESSAGE: {
                message.setContent(jsonObject.getString("content"));
                break;
            }
            case PokoMessage.MEMBER_EXIT: {
                message.setSpecialContent(message.getWriter().getNickname() + "님이 방을 나가셨습니다.");
                break;
            }
            default: {
                message.setContent(null);
                message.setSpecialContent(null);
                break;
            }
        }

        return message;
    }


    public static int parseMessageImportanceLevel(int level) throws ParseException {
        switch (level) {
            case 0:
                return PokoMessage.NORMAL;
            case 1:
                return PokoMessage.IMPORTANT;
            case 2:
                return PokoMessage.VERY_IMPORTANT;
            default:
                throw new ParseException("Unknown importance level", 1);
        }
    }

    public static PokoEvent parseEvent(JSONObject jsonObject) throws JSONException {
        DataCollection collection = DataCollection.getInstance();
        PokoEvent event = new PokoEvent();
        event.setEventId(jsonObject.getInt("eventId"));
        event.setEventName(jsonObject.getString("name"));
        event.setEventDate(Parser.epochInMillsToCalendar(jsonObject.getLong("date")));
        event.setAck(parseEventAck(jsonObject));
        event.setLocation(parseLocation(jsonObject));
        if (jsonObject.has("description") && !jsonObject.isNull("description")) {
            event.setDescription(jsonObject.getString("description"));
        } else {
            event.setDescription(null);
        }
        if (jsonObject.has("started") && !jsonObject.isNull("started")) {
            boolean started = jsonObject.getInt("started") > 0;
            if (started) {
                event.setState(PokoEvent.EVENT_STARTED);
            } else {
                event.setState(PokoEvent.EVENT_UPCOMING);
            }
        } else {
            event.setState(PokoEvent.EVENT_UPCOMING);
        }
        if (jsonObject.has("groupId") && !jsonObject.isNull("groupId")) {
            int groupId = jsonObject.getInt("groupId");
            Group group = DataCollection.getInstance().getGroupList().getItemByKey(groupId);
            if (group != null) {
                event.setGroup(group);
            }
        } else {
            event.setGroup(null);
        }
        if (jsonObject.has("participants") && !jsonObject.isNull("participants")) {
            JSONArray jsonParticipants = jsonObject.getJSONArray("participants");

            UserPokoList participants = event.getParticipants();
            for (int i = 0; i < jsonParticipants.length(); i++) {
                JSONObject jsonParticipant = (JSONObject) jsonParticipants.get(i);

                /* Get existing user or create new user */
                User participant = parseStranger(jsonParticipant);
                User exist = collection.getUserById(participant.getUserId());
                if (exist == null) {
                    collection.updateUserList(participant);
                } else {
                    participant = exist;
                }

                /* Add to participant */
                participants.updateItem(participant);
            }
        }
        if (jsonObject.has("creater") && !jsonObject.isNull("creater")) {
            JSONObject jsonCreator = jsonObject.getJSONObject("creater");

            /* Get existing user or create new user */
            User creator = parseStranger(jsonCreator);
            User exist = collection.getUserById(creator.getUserId());
            if (exist == null) {
                collection.updateUserList(creator);
            } else {
                creator = exist;
            }

            event.setCreator(creator);
        } else {
            throw new JSONException("Event creator must exist");
        }
        return event;
    }

    public static int parseEventAck(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has("acked") || jsonObject.isNull("acked")) {
            return PokoEvent.ACK_NOT_SEEN;
        }

        int acked = jsonObject.getInt("acked");

        switch (acked) {
            case SERVER_EVENT_ACK_NOT_SEEN: {
                return PokoEvent.ACK_NOT_SEEN;
            }
            case SERVER_EVENT_ACK_SEEN: {
                return PokoEvent.ACK_SEEN;
            }
            case SERVER_EVENT_ACK_SEEN_STARTED: {
                return PokoEvent.ACK_SEEN_STARTED;
            }
            default: {
                return PokoEvent.ACK_NOT_SEEN;
            }
        }
    }

    public static EventLocation parseLocation(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has("localization") || jsonObject.isNull("localization")) {
            return null;
        }

        JSONObject jsonLocal = jsonObject.getJSONObject("localization");
        EventLocation location = new EventLocation();
        if (jsonLocal.has("title") && !jsonLocal.isNull("title")) {
            location.setTitle(jsonLocal.getString("title"));
        } else {
            location.setTitle(Constants.unknownEventName);
        }
        if (jsonLocal.has("category") && !jsonLocal.isNull("category")) {
            location.setCategory(jsonLocal.getString("category"));
        } else {
            location.setCategory(null);
        }
        if (jsonLocal.has("description") && !jsonLocal.isNull("description")) {
            location.setAddress(jsonLocal.getString("description"));
        } else {
            location.setAddress(null);
        }
        if (jsonLocal.has("latitude") && jsonLocal.has("longitude")
                && !jsonLocal.isNull("latitude") && !jsonLocal.isNull("longitude")) {
            LatLng latLng = new LatLng(jsonLocal.getDouble("latitude"),
                    jsonLocal.getDouble("longitude"));
            location.setLatLng(latLng);
        } else {
            throw new JSONException("Event location does not have coordinates data");
        }
        if (jsonLocal.has("date") && !jsonLocal.isNull("date")) {
            Calendar date = Parser.epochInMillsToCalendar(jsonLocal.getLong("date"));

            location.setMeetingDate(date);
        } else {
            throw new JSONException("Event location does not have date data");
        }

        return location;
     }

    public static Calendar parseDateString(String dateStr) throws ParseException {
        SimpleDateFormat lastSeenFormat = new SimpleDateFormat(Constants.serverDateFormat, Locale.KOREA);
        lastSeenFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date lastSeenDate = lastSeenFormat.parse(dateStr);
        Calendar result = Calendar.getInstance();
        result.setTime(lastSeenDate);
        result.setTimeZone(Constants.timeZone);

        return result;
    }

    public static String formatCalendar(Calendar date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return dateFormat.format(date.getTime());
    }
}
