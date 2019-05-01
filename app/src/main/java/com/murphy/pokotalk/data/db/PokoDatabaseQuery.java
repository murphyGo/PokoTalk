package com.murphy.pokotalk.data.db;

import com.murphy.pokotalk.data.db.schema.ContactsSchema;
import com.murphy.pokotalk.data.db.schema.EventLocationSchema;
import com.murphy.pokotalk.data.db.schema.EventParticipantsSchema;
import com.murphy.pokotalk.data.db.schema.EventsSchema;
import com.murphy.pokotalk.data.db.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.db.schema.GroupsSchema;
import com.murphy.pokotalk.data.db.schema.MessagesSchema;
import com.murphy.pokotalk.data.db.schema.SessionSchema;
import com.murphy.pokotalk.data.db.schema.UsersSchema;

public class PokoDatabaseQuery {
    public String table;
    public QueryType queryType;
    public String[] projection;
    public String selection;
    public String sortOrder;
    public String limitOffset;

    public PokoDatabaseQuery(QueryType queryType, String table, String selection,
                             String sortOrder, String limitOffset) {
        this.queryType = queryType;
        this.table = table;
        this.projection = getProjection();
        this.selection = selection;
        this.sortOrder = sortOrder;
        this.limitOffset = limitOffset;
    }

    public String[] getProjection() {
        return null;
    }

    public enum QueryType {
        SELECT,
        INSERT,
        UPDATE,
        DELETE;
    }

    /** Queries */
    public static PokoDatabaseQuery insertSessionData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    SessionSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertUserData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    UsersSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertContactData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    ContactsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertGroupData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    GroupsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertGroupMemberData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    GroupMembersSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertEventParticipantData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    EventParticipantsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertMessageData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    MessagesSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertEventData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    EventsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery insertEventLocationData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    EventLocationSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery deleteAllSessionData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    SessionSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);


    public static PokoDatabaseQuery deleteAllContactData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    ContactsSchema.Entry.TABLE_NAME,
                    ContactsSchema.Entry.PENDING + " = 0",
                    null,
                    null);

    public static PokoDatabaseQuery deleteAllGroupData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    GroupsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery deleteAllGroupMemberData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    GroupMembersSchema.Entry.TABLE_NAME,
                    GroupMembersSchema.Entry.GROUP_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery deleteAllEventData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    EventsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null);

    public static PokoDatabaseQuery deleteAllEventParticipantData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    EventParticipantsSchema.Entry.TABLE_NAME,
                    EventParticipantsSchema.Entry.EVENT_ID + " = ?",
                    null,
                    null);


    public static PokoDatabaseQuery deleteAllPendingContactData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    ContactsSchema.Entry.TABLE_NAME,
                    ContactsSchema.Entry.PENDING + " = 1",
                    null,
                    null);

    public static PokoDatabaseQuery deleteContactData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    ContactsSchema.Entry.TABLE_NAME,
                    ContactsSchema.Entry.USER_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery deleteGroupData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    GroupsSchema.Entry.TABLE_NAME,
                    GroupsSchema.Entry.GROUP_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery deleteGroupMemberData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    GroupMembersSchema.Entry.TABLE_NAME,
                    GroupMembersSchema.Entry.GROUP_ID + " = ? and " +
                    GroupMembersSchema.Entry.USER_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery deleteEventData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    EventsSchema.Entry.TABLE_NAME,
                    EventsSchema.Entry.EVENT_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery deleteEventParticipantData =
            new PokoDatabaseQuery(QueryType.DELETE,
                    EventParticipantsSchema.Entry.TABLE_NAME,
                    EventParticipantsSchema.Entry.EVENT_ID + " = ? and " +
                            EventParticipantsSchema.Entry.PARTICIPANT_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateSessionData =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    SessionSchema.Entry.TABLE_NAME,
                    SessionSchema.Entry.USER_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateUserData =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    UsersSchema.Entry.TABLE_NAME,
                    UsersSchema.Entry.USER_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateContactChatDataNull =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    ContactsSchema.Entry.TABLE_NAME,
                    ContactsSchema.Entry.GROUP_CHAT_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateContactChatData =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    ContactsSchema.Entry.TABLE_NAME,
                    ContactsSchema.Entry.USER_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateGroup =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    GroupsSchema.Entry.TABLE_NAME,
                    GroupsSchema.Entry.GROUP_ID + " = ? ",
                    null,
                    null);

    public static PokoDatabaseQuery updateMessageInMessageIdRange =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    MessagesSchema.Entry.TABLE_NAME,
                    MessagesSchema.Entry.GROUP_ID + " = ? and " +
                            MessagesSchema.Entry.MESSAGE_ID + " >= ? and " +
                            MessagesSchema.Entry.MESSAGE_ID + " <= ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateMessageByMessageId =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    MessagesSchema.Entry.TABLE_NAME,
                    MessagesSchema.Entry.GROUP_ID + " = ? and " +
                            MessagesSchema.Entry.MESSAGE_ID + " = ?",
                    null,
                    null);

    public static PokoDatabaseQuery updateEventByEventId =
            new PokoDatabaseQuery(QueryType.UPDATE,
                    EventsSchema.Entry.TABLE_NAME,
                    EventsSchema.Entry.EVENT_ID + " = ?",
                    null,
                    null);


    public static PokoDatabaseQuery readSessionData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    SessionSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    "1") {
                    @Override
                    public String[] getProjection() {
                        String[] result = {SessionSchema.Entry.SESSION_ID,
                                SessionSchema.Entry.SESSION_EXPIRE,
                                SessionSchema.Entry.USER_ID,
                                SessionSchema.Entry.EMAIL,
                                SessionSchema.Entry.NICKNAME,
                                SessionSchema.Entry.PICTURE,
                                SessionSchema.Entry.LAST_SEEN,};
                        return result;
                    }
            };

    public static PokoDatabaseQuery readUserData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    UsersSchema.Entry.TABLE_NAME,
                    UsersSchema.Entry.USER_ID + " = ?",
                    null,
                    "1") {
                @Override
                public String[] getProjection() {
                    String[] result = {UsersSchema.Entry.USER_ID,
                            UsersSchema.Entry.EMAIL,
                            UsersSchema.Entry.NICKNAME,
                            UsersSchema.Entry.PICTURE,
                            UsersSchema.Entry.LAST_SEEN};
                    return result;
                }
            };

    public static String readAllUserData =
            "SELECT " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.USER_ID + ", " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.EMAIL + ", " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.NICKNAME + ", " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.PICTURE + ", " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.LAST_SEEN + ", " +
                    ContactsSchema.Entry.TABLE_NAME + "." + ContactsSchema.Entry.GROUP_CHAT_ID + ", " +
                    ContactsSchema.Entry.TABLE_NAME + "." + ContactsSchema.Entry.PENDING + ", " +
                    ContactsSchema.Entry.TABLE_NAME + "." + ContactsSchema.Entry.INVITED + " " +
                    "FROM " + UsersSchema.Entry.TABLE_NAME + " LEFT JOIN " +
                    ContactsSchema.Entry.TABLE_NAME + " ON " +
                    UsersSchema.Entry.TABLE_NAME + "." + UsersSchema.Entry.USER_ID + " = " +
                    ContactsSchema.Entry.TABLE_NAME + "." + ContactsSchema.Entry.USER_ID;

    public static PokoDatabaseQuery readAllUserDataTEST =
            new PokoDatabaseQuery(QueryType.SELECT,
                    UsersSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {UsersSchema.Entry.USER_ID,
                            UsersSchema.Entry.NICKNAME,
                            UsersSchema.Entry.EMAIL,
                            UsersSchema.Entry.PICTURE};
                    return result;
                }
            };

    public static PokoDatabaseQuery readGroupData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    GroupsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {GroupsSchema.Entry.GROUP_ID,
                            GroupsSchema.Entry.NAME,
                            GroupsSchema.Entry.ALIAS,
                            GroupsSchema.Entry.NB_NEW_MESSAGES,
                            GroupsSchema.Entry.ACK};
                    return result;
                }
            };

    public static PokoDatabaseQuery readGroupMemberData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    GroupMembersSchema.Entry.TABLE_NAME,
                    GroupMembersSchema.Entry.GROUP_ID + " = ?",
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {GroupMembersSchema.Entry.GROUP_ID,
                            GroupMembersSchema.Entry.USER_ID};
                    return result;
                }
            };

    public static PokoDatabaseQuery readMessageDataFromBack =
            new PokoDatabaseQuery(QueryType.SELECT,
                    MessagesSchema.Entry.TABLE_NAME,
                    MessagesSchema.Entry.GROUP_ID + " = ?",
                    MessagesSchema.Entry.MESSAGE_ID + " desc",
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {MessagesSchema.Entry.MESSAGE_ID,
                            MessagesSchema.Entry.MESSAGE_TYPE,
                            MessagesSchema.Entry.IMPORTANCE,
                            MessagesSchema.Entry.USER_ID,
                            MessagesSchema.Entry.DATE,
                            MessagesSchema.Entry.CONTENTS,
                            MessagesSchema.Entry.SPECIAL_CONTENTS,
                            MessagesSchema.Entry.NB_NOT_READ};
                    return result;
                }
            };

    public static PokoDatabaseQuery readMessageDataFromBackByMessageId =
            new PokoDatabaseQuery(QueryType.SELECT,
                    MessagesSchema.Entry.TABLE_NAME,
                    MessagesSchema.Entry.GROUP_ID + " = ? and " +
                    MessagesSchema.Entry.MESSAGE_ID + " <= ?",
                    MessagesSchema.Entry.MESSAGE_ID + " desc",
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {MessagesSchema.Entry.MESSAGE_ID,
                            MessagesSchema.Entry.MESSAGE_TYPE,
                            MessagesSchema.Entry.IMPORTANCE,
                            MessagesSchema.Entry.USER_ID,
                            MessagesSchema.Entry.DATE,
                            MessagesSchema.Entry.CONTENTS,
                            MessagesSchema.Entry.SPECIAL_CONTENTS,
                            MessagesSchema.Entry.NB_NOT_READ};
                    return result;
                }
            };

    public static PokoDatabaseQuery readEventData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    EventsSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {EventsSchema.Entry.EVENT_ID,
                            EventsSchema.Entry.EVENT_NAME,
                            EventsSchema.Entry.EVENT_CREATOR,
                            EventsSchema.Entry.EVENT_DESCRIPTION,
                            EventsSchema.Entry.EVENT_DATE,
                            EventsSchema.Entry.EVENT_STARTED,
                            EventsSchema.Entry.ACK};
                    return result;
                }
            };

    public static PokoDatabaseQuery readEventParticipantData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    EventParticipantsSchema.Entry.TABLE_NAME,
                    EventParticipantsSchema.Entry.EVENT_ID + " = ?",
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {EventParticipantsSchema.Entry.EVENT_ID,
                            EventParticipantsSchema.Entry.PARTICIPANT_ID};
                    return result;
                }
            };

    public static PokoDatabaseQuery readEventLocationData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    EventLocationSchema.Entry.TABLE_NAME,
                    EventLocationSchema.Entry.EVENT_ID + " = ?",
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {EventLocationSchema.Entry.EVENT_ID,
                            EventLocationSchema.Entry.LOCATION_TITLE,
                            EventLocationSchema.Entry.LOCATION_CATEGORY,
                            EventLocationSchema.Entry.LOCATION_ADDRESS,
                            EventLocationSchema.Entry.LOCATION_MEETING_DATE,
                            EventLocationSchema.Entry.LOCATION_LATITUDE,
                            EventLocationSchema.Entry.LOCATION_LONGITUDE,
                    };
                    return result;
                }
            };
}
