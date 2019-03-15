package com.murphy.pokotalk.data.file;

import com.murphy.pokotalk.data.file.schema.ContactsSchema;
import com.murphy.pokotalk.data.file.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.file.schema.GroupsSchema;
import com.murphy.pokotalk.data.file.schema.MessagesSchema;
import com.murphy.pokotalk.data.file.schema.SessionSchema;
import com.murphy.pokotalk.data.file.schema.UsersSchema;

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

    public static PokoDatabaseQuery insertMessageData =
            new PokoDatabaseQuery(QueryType.INSERT,
                    MessagesSchema.Entry.TABLE_NAME,
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
                                SessionSchema.Entry.USER_ID};
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
            "SELECT * " +
                    "FROM Users LEFT JOIN Contacts ON Users.userId = Contacts.userId";

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
}
