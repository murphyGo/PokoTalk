package com.murphy.pokotalk.data.file;

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
    public static PokoDatabaseQuery readSessionData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    SessionSchema.Entry.TABLE_NAME,
                    null,
                    null,
                    "1") {
                    @Override
                    public String[] getProjection() {
                        String[] result = {SessionSchema.Entry.USER_ID,
                                SessionSchema.Entry.SESSION_ID};
                        return result;
                    }
            };

    public static PokoDatabaseQuery readUserData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    UsersSchema.Entry.TABLE_NAME,
                    "userId = ?",
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
                    "FROM Users LEFT JOIN Contacts ON Users.userId = Contacts.userId " +
                    "WHERE Contacts.pending = 0";

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
                    "groupId = ?",
                    null,
                    null) {
                @Override
                public String[] getProjection() {
                    String[] result = {GroupMembersSchema.Entry.GROUP_ID,
                            GroupMembersSchema.Entry.USER_ID};
                    return result;
                }
            };

    public static PokoDatabaseQuery readMessageData =
            new PokoDatabaseQuery(QueryType.SELECT,
                    MessagesSchema.Entry.TABLE_NAME,
                    "groupId = ?",
                    "messageId desc",
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
