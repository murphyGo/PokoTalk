package com.murphy.pokotalk.data.file;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.schema.ContactsSchema;
import com.murphy.pokotalk.data.file.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.file.schema.SessionSchema;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerList;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

/** PokoDataabseManager reads and writes from/to database on device of user. */
public class PokoDatabaseManager {
    public static Session loadSessionData(Context context) {
        PokoDatabase database = PokoDatabase.getInstance(context);

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.query(db, PokoDatabaseQuery.readSessionData, null);

        try {
            if (!cursor.moveToNext()) {
                return null;
            }

            String sessionId = cursor.getString(cursor.getColumnIndexOrThrow(SessionSchema.Entry.SESSION_ID));
            long expire = cursor.getLong(cursor.getColumnIndexOrThrow(SessionSchema.Entry.SESSION_EXPIRE));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(SessionSchema.Entry.USER_ID));
            cursor.close();

            String[] selectionArgs = {Integer.toString(userId)};
            cursor = PokoDatabaseHelper.query(db, PokoDatabaseQuery.readUserData, selectionArgs);

            if (!cursor.moveToNext()) {
                return null;
            }

            /* Set session and user objects */
            Session session = Session.getInstance();
            session.setSessionExpire(Parser.epochInMillsToCalendar(expire));
            session.setSessionId(sessionId);

            Contact user = Parser.parseUser(cursor);
            session.setUser(user);

            return session;
        } finally {
            cursor.close();
        }
    }

    public static void loadUserData(Context context) {
        User user = Session.getInstance().getUser();

        // Get user lists
        DataCollection collection = DataCollection.getInstance();
        ContactList contactList = collection.getContactList();
        PendingContactList invitedPendingContactList = collection.getInvitedContactList();
        PendingContactList invitingPendingContactList = collection.getInvitingContactList();
        StrangerList strangerList = collection.getStrangerList();

        PokoDatabase database = PokoDatabase.getInstance(context);

        // Query user data
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.readAllUserData(db);

        // Get indexes for attributes
        int userIdIndex = cursor.getColumnIndexOrThrow(ContactsSchema.Entry.USER_ID);
        int pendingIndex = cursor.getColumnIndexOrThrow(ContactsSchema.Entry.PENDING);
        int invitedIndex = cursor.getColumnIndexOrThrow(ContactsSchema.Entry.INVITED);
        int groupChatIdIndex = cursor.getColumnIndexOrThrow(ContactsSchema.Entry.GROUP_CHAT_ID);

        try {
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(userIdIndex);

                // Ignore the session user
                if (userId == user.getUserId()) {
                    continue;
                }

                if (cursor.isNull(pendingIndex)) {
                    // Stranger will have pending attribute Null
                    Stranger stranger = Parser.parseStranger(cursor);
                    strangerList.updateItem(stranger);
                } else {
                    int pending = cursor.getInt(pendingIndex);
                    if (pending > 0) {
                        // Pending contact
                        // Test if it is a invited or inviting pending contact
                        int invited = cursor.getInt(invitedIndex);
                        PendingContact pendingContact = Parser.parsePendingContact(cursor);
                        if (invited > 0) {
                            invitedPendingContactList.updateItem(pendingContact);
                        } else {
                            invitingPendingContactList.updateItem(pendingContact);
                        }
                    } else {
                        // Contact
                        Contact contact = Parser.parseContact(cursor);
                        contactList.updateItem(contact);

                        // See contact group chat data
                        if (!cursor.isNull(groupChatIdIndex)) {
                            int contactGroupId = cursor.getInt(groupChatIdIndex);
                            contactList.putContactGroupRelation(contact.getUserId(), contactGroupId);
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
    }

    public static void loadGroupData(Context context) throws Exception {
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();

        PokoDatabase database = PokoDatabase.getInstance(context);

        // Query groups
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.readGroupData(db);

        try {
            // Parse groups
            while(cursor.moveToNext()) {
                Group group = Parser.parseGroup(cursor);
                groupList.updateItem(group);
            }
        } finally {
            cursor.close();
        }

        // Loop for each group
        for (Group group : groupList.getList()) {
            UserList memberList = group.getMembers();
            MessageList messageList = group.getMessageList();

            // Query group members and last message
            Cursor memberCursor = PokoDatabaseHelper.readGroupMemberData(db, group.getGroupId());
            Cursor messageCursor = PokoDatabaseHelper.readMessageData(db, group.getGroupId(), 0 , 1);

            // Get indexes for attributes
            int userIdIndex = memberCursor.getColumnIndexOrThrow(GroupMembersSchema.Entry.USER_ID);

            // Parse member data
            try {
                while (memberCursor.moveToNext()) {
                    int userId = memberCursor.getInt(userIdIndex);
                    User member = collection.getUserById(userId);
                    if (member == null) {
                        throw new Exception("Can not find group member");
                    }

                    memberList.updateItem(member);
                }
            } finally {
                memberCursor.close();
            }

            // Parse message data
            try {
                if (messageCursor.moveToNext()) {
                    PokoMessage message = Parser.parseMessage(cursor);

                    messageList.updateItem(message);
                }
            } finally {
                messageCursor.close();
            }
        }
    }
}
