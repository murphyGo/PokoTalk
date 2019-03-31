package com.murphy.pokotalk.data.file.json;

import android.content.ContentValues;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.schema.ContactsSchema;
import com.murphy.pokotalk.data.file.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.file.schema.GroupsSchema;
import com.murphy.pokotalk.data.file.schema.MessagesSchema;
import com.murphy.pokotalk.data.file.schema.SessionSchema;
import com.murphy.pokotalk.data.file.schema.UsersSchema;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserPokoList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Serializer {

    public static ContentValues obtainSessionValues(Session session) {
        ContentValues values = new ContentValues();
        values.put(SessionSchema.Entry.SESSION_ID, session.getSessionId());
        values.put(SessionSchema.Entry.SESSION_EXPIRE, calendarToEpochInMills(session.getSessionExpire()));
        values.put(SessionSchema.Entry.USER_ID, session.getUser().getUserId());

        return values;
    }

    public static ContentValues obtainUserValues(User user) {
        ContentValues values = new ContentValues();
        String picture = user.getPicture();
        Calendar lastSeen = null;
        if (user instanceof Contact) {
            lastSeen = ((Contact) user).getLastSeen();
        }

        values.put(UsersSchema.Entry.USER_ID, user.getUserId());
        values.put(UsersSchema.Entry.EMAIL, user.getEmail());
        values.put(UsersSchema.Entry.NICKNAME, user.getNickname());
        if (picture != null) {
            values.put(UsersSchema.Entry.PICTURE, picture);
        }

        if (lastSeen != null) {
            values.put(UsersSchema.Entry.LAST_SEEN, calendarToEpochInMills(lastSeen));
        }

        return values;
    }

    public static ContentValues obtainContactValues(User user, boolean isInvited) {
        ContentValues values = new ContentValues();
        int pending = 0;
        int invited = isInvited ? 1 : 0;
        Integer groupChatId = null;
        if (user instanceof Contact) {
            Contact contact = (Contact) user;
            ContactPokoList contactList = DataCollection.getInstance().getContactList();
            ContactPokoList.ContactGroupRelation relation =
                    contactList.getContactGroupRelationByUserId(contact.getUserId());

            if (relation != null) {
                groupChatId = relation.getGroupId();
            }

        } else if (user instanceof PendingContact) {
            pending = 1;
        }

        values.put(ContactsSchema.Entry.USER_ID, user.getUserId());
        values.put(ContactsSchema.Entry.PENDING, pending);
        values.put(ContactsSchema.Entry.INVITED, invited);
        if (groupChatId != null) {
            values.put(ContactsSchema.Entry.GROUP_CHAT_ID, groupChatId);
        }

        return values;
    }

    public static ContentValues obtainGroupValues(Group group) {
        ContentValues values = new ContentValues();
        String name = group.getGroupName();
        String alias = group.getAlias();

        values.put(GroupsSchema.Entry.GROUP_ID, group.getGroupId());
        values.put(GroupsSchema.Entry.NB_NEW_MESSAGES, group.getNbNewMessages());
        values.put(GroupsSchema.Entry.ACK, group.getAck());
        if (name != null) {
            values.put(GroupsSchema.Entry.NAME, group.getGroupName());
        }

        if (alias != null) {
            values.put(GroupsSchema.Entry.ALIAS, group.getAlias());
        }

        return values;
    }

    public static ContentValues obtainGroupMemberValues(Group group, User user) {
        ContentValues values = new ContentValues();

        values.put(GroupMembersSchema.Entry.GROUP_ID, group.getGroupId());
        values.put(GroupMembersSchema.Entry.USER_ID, user.getUserId());

        return values;
    }

    public static ContentValues obtainMessageValues(Group group, PokoMessage message) {
        ContentValues values = new ContentValues();

        values.put(MessagesSchema.Entry.GROUP_ID, group.getGroupId());
        values.put(MessagesSchema.Entry.MESSAGE_ID, message.getMessageId());
        values.put(MessagesSchema.Entry.MESSAGE_TYPE, message.getMessageType());
        values.put(MessagesSchema.Entry.USER_ID, message.getWriter().getUserId());
        values.put(MessagesSchema.Entry.NB_NOT_READ, message.getNbNotReadUser());
        values.put(MessagesSchema.Entry.IMPORTANCE, message.getImportanceLevel());
        values.put(MessagesSchema.Entry.DATE, calendarToEpochInMills(message.getDate()));
        values.put(MessagesSchema.Entry.CONTENTS, message.getContent());
        values.put(MessagesSchema.Entry.SPECIAL_CONTENTS, message.getSpecialContent());

        return values;
    }

    public static JSONObject makeSessionJSON(Session session) throws JSONException {
        JSONObject jsonSession = new JSONObject();
        jsonSession.put("sessionId", session.getSessionId());
        jsonSession.put("sessionExpire", calendarToEpochInMills(session.getSessionExpire()));
        JSONObject jsonUser = makeUserJSON(session.getUser());
        jsonSession.put("user", jsonUser);

        return jsonSession;
    }

    public static JSONObject makeUserJSON(Contact user) throws JSONException {
        return makeContactJSON(user);
    }

    public static long calendarToEpochInMills(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    public static JSONObject makeContactJSON(Contact contact) throws JSONException {
        JSONObject jsonUser = makeStrangerJSON(contact);
        jsonUser.put("lastSeen", calendarToEpochInMills(contact.getLastSeen()));

        return jsonUser;
    }

    public static JSONObject makePendingContactJSON(PendingContact pendingContact) throws JSONException {
        JSONObject jsonUser = makeStrangerJSON(pendingContact);

        return jsonUser;
    }

    public static JSONObject makeStrangerJSON(User user) throws JSONException {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("userId", user.getUserId());
        jsonUser.put("email", user.getEmail());
        jsonUser.put("nickname", user.getNickname());
        jsonUser.put("picture", user.getPicture());

        return jsonUser;
    }

    public static JSONObject makeContactGroupRelationJSON(ContactPokoList.ContactGroupRelation relation)
        throws JSONException {
        JSONObject jsonRelation = new JSONObject();
        jsonRelation.put("contactUserId", relation.getContactUserId());
        jsonRelation.put("groupId", relation.getGroupId());

        return jsonRelation;
    }

    public static JSONObject makeGroupJSON(Group group) throws JSONException {
        JSONObject jsonGroup = new JSONObject();
        JSONArray jsonMembers = new JSONArray();

        UserPokoList userList = group.getMembers();
        ArrayList<User> members = userList.getList();

        jsonGroup.put("groupId", group.getGroupId());
        jsonGroup.put("groupName", group.getGroupName());
        jsonGroup.put("alias", group.getAlias());
        jsonGroup.put("nbNewMessages", group.getNbNewMessages());
        jsonGroup.put("ack", group.getAck());

        for (int i = 0; i < members.size(); i++) {
            User member = members.get(i);
            jsonMembers.put(makeMemberJSON(member));
        }

        jsonGroup.put("members", jsonMembers);

        return jsonGroup;
    }

    public static JSONObject makeMemberJSON(User member) throws JSONException {
        JSONObject jsonMember = new JSONObject();
        jsonMember.put("userId", member.getUserId());

        return jsonMember;
    }

    public static JSONObject makeMessageJSON(PokoMessage message) throws JSONException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("messageId", message.getMessageId());
        jsonMessage.put("messageType", message.getMessageType());
        jsonMessage.put("date", calendarToEpochInMills(message.getDate()));
        jsonMessage.put("importanceLevel", message.getImportanceLevel());
        jsonMessage.put("content", message.getContent());
        jsonMessage.put("specialContent", message.getSpecialContent());
        jsonMessage.put("nbNotReadUser", message.getNbNotReadUser());
        jsonMessage.put("writer", makeMemberJSON(message.getWriter()));

        return jsonMessage;
    }
}
