package com.murphy.pokotalk.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.schema.UsersSchema;
import com.murphy.pokotalk.data.event.EventList;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.db.schema.ContactsSchema;
import com.murphy.pokotalk.data.db.schema.EventParticipantsSchema;
import com.murphy.pokotalk.data.db.schema.GroupMembersSchema;
import com.murphy.pokotalk.data.db.schema.SessionSchema;
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

import java.util.ArrayDeque;
import java.util.HashMap;

/** PokoDataabseManager reads and writes from/to database on device of user. */
public class PokoDatabaseManager {
    protected ArrayDeque<JobSchedule> jobQueue;
    protected PokoAsyncDatabaseJob runningJob;
    protected static PokoDatabaseManager instance = null;
    protected int state = ENABLED;

    public static final int ENABLED = 1;
    public static final int DISABLED = 2;

    public PokoDatabaseManager() {
        runningJob = null;
        jobQueue = new ArrayDeque<>();
    }

    public static PokoDatabaseManager getInstance() {
        if (instance == null) {
            instance = new PokoDatabaseManager();
        }

        return instance;
    }

    public synchronized void disable() {
        state = DISABLED;

        // Discard all the job in the queue
        jobQueue.clear();

        // Check if there is currently running job
        if (runningJob != null) {
            // Wait for running job to be done
            while (true) {
                try {
                    wait();
                    break;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }

    public synchronized void enable() {
        state = ENABLED;
    }

    public synchronized void enqueueJob(PokoAsyncDatabaseJob job, HashMap<String, Object> data) {
        if (state == DISABLED || job == null) {
            return;
        }

        JobSchedule schedule = new JobSchedule(job, data);
        if (runningJob == null && jobQueue.size() == 0) {
            startJob(schedule);
        } else {
            jobQueue.addLast(schedule);
        }
    }

    public synchronized void startNextJob() {
        runningJob = null;
        if (jobQueue.size() > 0) {
            JobSchedule schedule = jobQueue.removeFirst();
            startJob(schedule);
        }

        if (state == DISABLED) {
            notifyAll();
        }
    }

    public synchronized void startJob(JobSchedule entry) {
        if (runningJob == null) {
            runningJob = entry.job;
            entry.job.execute(entry.data);
        }
    }

    class JobSchedule {
        PokoAsyncDatabaseJob job;
        HashMap<String, Object> data;

        public JobSchedule(PokoAsyncDatabaseJob job, HashMap<String, Object> data) {
            this.job = job;
            this.data = data;
        }
    }

    /** These methods accesses to application data structure.
     * So data lock must be acquired before calling these methods.
     */
    public static Session loadSessionData(Context context) {
        PokoSessionDatabase database = PokoSessionDatabase.getInstance(context);

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor sessionCursor = PokoDatabaseHelper.query(db, PokoDatabaseQuery.readSessionData, null);

        try {
            if (!sessionCursor.moveToNext()) {
                return null;
            }

            String sessionId = sessionCursor.getString(
                    sessionCursor.getColumnIndexOrThrow(SessionSchema.Entry.SESSION_ID));
            long expire = sessionCursor.getLong(
                    sessionCursor.getColumnIndexOrThrow(SessionSchema.Entry.SESSION_EXPIRE));

            // Set session and user objects
            Session session = Session.getInstance();
            session.setSessionExpire(Parser.epochInMillsToCalendar(expire));
            session.setSessionId(sessionId);

            // Parse user data
            Contact user = Parser.parseUser(sessionCursor);
            Contact oldUser = session.getUser();
            if (oldUser == null) {
                session.setUser(user);
            } else {
                oldUser.update(user);
            }

            return session;
        } finally {
            sessionCursor.close();

            db.releaseReference();
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

        PokoUserDatabase database = PokoUserDatabase.getInstance(context, user.getUserId());

        // Query user data
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.readAllUserData(db);

        // Get indexes for attributes
        int userIdIndex = cursor.getColumnIndexOrThrow(UsersSchema.Entry.USER_ID);
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

                    Log.v("POKO", "READ STRANGER " + stranger.getNickname() +
                            ", " + stranger.getUserId());
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

                        Log.v("POKO", "READ PENDING CONTACT " +
                                pendingContact.getNickname() + ", " + pendingContact.getUserId());
                    } else {
                        // Contact
                        Contact contact = Parser.parseContact(cursor);
                        contactList.updateItem(contact);

                        Log.v("POKO", "READ CONTACT " + contact.getNickname() + ", " +
                                contact.getUserId());

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

            db.releaseReference();
        }
    }

    public static void loadGroupData(Context context) throws Exception {
        User user = Session.getInstance().getUser();
        DataCollection collection = DataCollection.getInstance();
        GroupList groupList = collection.getGroupList();

        PokoUserDatabase database = PokoUserDatabase.getInstance(context, user.getUserId());

        // Query groups
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.readGroupData(db);

        try {
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
                Cursor messageCursor = PokoDatabaseHelper.
                        readMessageDataFromBack(db, group.getGroupId(), 0 , 1);

                // Get indexes for attributes
                int userIdIndex = memberCursor.getColumnIndexOrThrow(GroupMembersSchema.Entry.USER_ID);

                // Parse member data
                try {
                    while (memberCursor.moveToNext()) {
                        int userId = memberCursor.getInt(userIdIndex);
                        User member = collection.getUserById(userId);
                        if (member == null) {
                            Log.e("POKO", "Can not find group member");
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
                        PokoMessage message = Parser.parseMessage(messageCursor);

                        messageList.updateItem(message);
                    }
                } finally {
                    messageCursor.close();
                }
            }
        } finally {
            db.releaseReference();
        }
    }

    public static void loadEventData(Context context) throws Exception {
        User user = Session.getInstance().getUser();
        DataCollection collection = DataCollection.getInstance();
        EventList eventList = collection.getEventList();

        PokoUserDatabase database = PokoUserDatabase.getInstance(context, user.getUserId());

        // Query events
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = PokoDatabaseHelper.readEventData(db);

        try {
            try {
                // Parse events
                while(cursor.moveToNext()) {
                    PokoEvent event = Parser.parseEvent(cursor);
                    eventList.updateItem(event);
                }
            } finally {
                cursor.close();
            }

            // Loop for each group
            for (PokoEvent event : eventList.getList()) {
                UserList participantList = event.getParticipants();

                // Get event id
                int eventId = event.getEventId();

                // Query event participants and location
                Cursor participantCursor = PokoDatabaseHelper.readEventParticipantData(db, eventId);
                Cursor locationCursor = PokoDatabaseHelper.readEventLocationData(db, eventId);

                // Get indexes for attributes
                int userIdIndex = participantCursor.
                        getColumnIndexOrThrow(EventParticipantsSchema.Entry.PARTICIPANT_ID);

                // Parse participant data
                try {
                    while (participantCursor.moveToNext()) {
                        int userId = participantCursor.getInt(userIdIndex);
                        User participant = collection.getUserById(userId);
                        if (participant == null) {
                            Log.e("POKO", "Can not find event participant");
                            throw new Exception("Can not find event participant");
                        }

                        participantList.updateItem(participant);
                    }
                } finally {
                    participantCursor.close();
                }

                // Parse location data
                try {
                    if (locationCursor.moveToNext()) {
                        EventLocation location = Parser.parseEventLocation(locationCursor);

                        event.setLocation(location);
                    } else {
                        event.setLocation(null);
                    }
                } finally {
                    locationCursor.close();
                }
            }
        } finally {
            db.releaseReference();
        }
    }
}
