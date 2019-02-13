package com.murphy.pokotalk.data.file.json;

import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public static JSONObject makeGroupJSON(Group group) throws JSONException {
        JSONObject jsonGroup = new JSONObject();
        JSONArray jsonMembers = new JSONArray();

        UserList userList = group.getMembers();
        ArrayList<User> members = userList.getList();

        jsonGroup.put("groupId", group.getGroupId());
        jsonGroup.put("groupName", group.getGroupName());
        jsonGroup.put("alias", group.getAlias());
        jsonGroup.put("nbNewMessages", group.getNbNewMessages());

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

    public static JSONObject makeMessageJSON(Message message) throws JSONException {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("messageId", message.getMessageId());
        jsonMessage.put("messageType", message.getMessageType());
        jsonMessage.put("date", calendarToEpochInMills(message.getDate()));
        jsonMessage.put("importanceLevel", message.getImportanceLevel());
        jsonMessage.put("content", message.getContent());
        jsonMessage.put("nbNotReadUser", message.getNbNotReadUser());
        jsonMessage.put("writer", makeMemberJSON(message.getWriter()));
        jsonMessage.put("acked", message.isAcked());

        return jsonMessage;
    }
}
