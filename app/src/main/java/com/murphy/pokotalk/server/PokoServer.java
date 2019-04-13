package com.murphy.pokotalk.server;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;
import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseManager;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.listener.chat.AckMessageListener;
import com.murphy.pokotalk.listener.chat.GetMemberJoinHistory;
import com.murphy.pokotalk.listener.chat.MessageAckListener;
import com.murphy.pokotalk.listener.chat.NewMessageListener;
import com.murphy.pokotalk.listener.chat.ReadMessageListener;
import com.murphy.pokotalk.listener.chat.ReadNbreadOfMessages;
import com.murphy.pokotalk.listener.chat.SendMessageListener;
import com.murphy.pokotalk.listener.connection.OnConnectionListener;
import com.murphy.pokotalk.listener.connection.OnDisconnectionListener;
import com.murphy.pokotalk.listener.contact.ContactChatRemovedListener;
import com.murphy.pokotalk.listener.contact.ContactDeniedListener;
import com.murphy.pokotalk.listener.contact.ContactRemovedListener;
import com.murphy.pokotalk.listener.contact.GetContactListListener;
import com.murphy.pokotalk.listener.contact.GetPendingContactListListener;
import com.murphy.pokotalk.listener.contact.JoinContactChatListener;
import com.murphy.pokotalk.listener.contact.NewContactListener;
import com.murphy.pokotalk.listener.contact.NewPendingContactListener;
import com.murphy.pokotalk.listener.event.EventAckListener;
import com.murphy.pokotalk.listener.event.EventCreatedListener;
import com.murphy.pokotalk.listener.event.EventExitListener;
import com.murphy.pokotalk.listener.event.EventParticipantExitedListener;
import com.murphy.pokotalk.listener.event.EventStartedListener;
import com.murphy.pokotalk.listener.event.GetEventListListener;
import com.murphy.pokotalk.listener.group.AddGroupListener;
import com.murphy.pokotalk.listener.group.ExitGroupListener;
import com.murphy.pokotalk.listener.group.GetGroupListListener;
import com.murphy.pokotalk.listener.group.MembersExitListener;
import com.murphy.pokotalk.listener.group.MembersInvitedListener;
import com.murphy.pokotalk.listener.session.AccountRegisteredListener;
import com.murphy.pokotalk.listener.session.PasswordLoginListener;
import com.murphy.pokotalk.listener.session.SessionLoginListener;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokoServer extends ServerSocket {
    protected static PokoServer pokoServer = null;
    protected int state = IDLE;

    public static final int IDLE = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 3;
    
    public PokoServer() {
        super(Constants.serverURL);
    }

    /* There is only one connection so we use singleton design
    * Socket try to connect only when getInstance method is called with context not null */
    public static PokoServer getInstance() {
        if (pokoServer == null) {
            synchronized (PokoServer.class) {
                pokoServer = pokoServer == null ? new PokoServer(): pokoServer;
            }
        }
        
        return pokoServer;
    }
    
    // Connect to server
    public void connect(Context context) {
        try {
            if (state == IDLE && context != null) {
                createSocket(context);
                enrollOnMessageHandlers();
                connect();
                state = CONNECTING;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Socket error", e.toString());
        }
    }

    // Disconnect from server
    @Override
    public void disconnect() {
        super.disconnect();
        
        state = IDLE;
    }

    private static abstract class EventListener implements Emitter.Listener {
        protected HashMap<String, Object> data;
        private String eventName;

        public EventListener() {
            data = new HashMap<>();
        }

        public void putData(String key, Object value) {
            data.put(key, value);
        }

        public Object getData(String key) {
            return data.get(key);
        }

        @Override
        public void call(Object... args) {
            eventName = getEventName();
        }

        public abstract String getEventName();
    }

    /* Socket event listener */
    public static abstract class SocketEventListener extends EventListener {
        @Override
        public void call(Object... args) {
            super.call(args);

            // Create redundant status
            Status status = new Status(Status.SUCCESS);

            call(status, args);
            PokoServer.getInstance().startActivityCallbacks(getEventName(), status, data, args);
        }

        public abstract void call(Status status, Object... args);
    }

    /* Application event listener */
    public static abstract class PokoListener extends EventListener {
        @Override
        public void call(Object... args) {
            super.call(args);

            JSONObject data = (JSONObject) args[0];
            try {
                String statusString = data.getString("status").toLowerCase();
                Status status = null;

                //TODO: status should contain detailed error codes and message
                if (TextUtils.equals(statusString, "success")) {
                    status = new Status(Status.SUCCESS);
                } else if (TextUtils.equals(statusString, "fail")) {
                    status = new Status(Status.ERROR);
                } else {
                    Log.e("Poko", "Bad status type");
                    return;
                }

                DataLock.getInstance().acquireWriteLock();
                try {
                    Log.v("SERVER DATA " + getEventName(), data.toString());
                    Log.v("HANDLING THREAD " + getEventName(), ""+ Thread.currentThread().getId());
                    /* Start application callback */
                    if (status.isSuccess()) {
                        callSuccess(status, args);
                    } else {
                        callError(status, args);
                    }

                    /* If status is success and there is a database job,
                     * start job. */
                    PokoAsyncDatabaseJob job = getDatabaseJob();
                    if (status.isSuccess() && job != null) {
                        PokoDatabaseManager.getInstance().enqueueJob(job, this.data);
                    }

                    /* Start callbacks given by activities */
                    PokoServer.getInstance().
                            startActivityCallbacks(getEventName(), status, this.data, args);

                } finally {
                    DataLock.getInstance().releaseWriteLock();
                }
            } catch(JSONException e) {
                Log.e("Poko", "Bad json status data");
                return;
            } catch (InterruptedException e) {
                Log.e("Poko", "Lock wait interrupted");
            }
        }

        public abstract void callSuccess(Status status, Object... args);
        public abstract void callError(Status status, Object... args);
        public abstract PokoAsyncDatabaseJob getDatabaseJob();
    }

    /* Methods for message send */
    public void sendAccountRegister(String name, String email, String password) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.accountRegisteredName, jsonData);
    }

    public void sendPasswordLogin(String email, String password) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        data.put("password", password);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.passwordLoginName, jsonData);
    }

    public void sendSessionLogin(String sessionId) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("sessionId", sessionId);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.sessionLoginName, jsonData);
    }

    public void sendGetContactList() {
        socket.emit(Constants.getContactListName);
    }

    public void sendGetPendingContactList() {
        socket.emit(Constants.getPendingContactListName);
    }

    public void sendAddContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.addContactName, jsonData);
    }

    public void sendRemoveContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.removeContactName, jsonData);
    }

    public void sendAcceptContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.acceptContactName, jsonData);
    }

    public void sendDenyContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.denyContactName, jsonData);
    }

    public void sendJoinContactChat(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        socket.emit(Constants.joinContactChatName, jsonData);
    }

    public void sendGetGroupList() {
        socket.emit(Constants.getGroupListName);
    }

    public void sendAddGroup(String name, List<String> emails) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            JSONArray jsonEmailArray = new JSONArray(emails);
            jsonData.put("members", jsonEmailArray);
            socket.emit(Constants.addGroupName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendExitGroup(int groupId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            socket.emit(Constants.exitGroupName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNewMessage(int groupId, int sendId, String content,
                            @Nullable Integer importanceLevel) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("sendId", sendId);
            jsonData.put("content", content);
            jsonData.put("importance", importanceLevel);
            socket.emit(Constants.sendMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendReadMessage(int groupId, int startMessageId, int nbMessageMax) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("startMessageId", startMessageId);
            jsonData.put("nbMessageMax", nbMessageMax);
            socket.emit(Constants.readMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendReadNbreadOfMessages(int groupId, int startMessageId, int endMessageId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("startMessageId", startMessageId);
            jsonData.put("endMessageId", endMessageId);
            socket.emit(Constants.readNbreadOfMessages, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendInviteGroupMembers(int groupId, List<String> emails) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            JSONArray jsonEmailArray = new JSONArray(emails);
            jsonData.put("members", jsonEmailArray);
            socket.emit(Constants.inviteGroupMembersName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendAckMessage(int groupId, int fromId, int toId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("ackStart", fromId);
            jsonData.put("ackEnd", toId);
            socket.emit(Constants.ackMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendGetMemberJoinHistory(int groupId, int messageId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("messageId", messageId);
            socket.emit(Constants.getMemberJoinHistory, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendGetEventList() {
        socket.emit(Constants.getEventListName);
    }

    public void sendCreateEvent(String name, String description, List<String> emails,
                                Calendar date, EventLocation location) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            jsonData.put("description", description);
            jsonData.put("date", date.getTime().getTime());
            JSONArray jsonEmailArray = new JSONArray(emails);
            jsonData.put("participants", jsonEmailArray);

            if (location != null) {
                JSONObject jsonLocalization = new JSONObject();
                LatLng latLng = location.getLatLng();
                jsonLocalization.put("title", location.getTitle());
                jsonLocalization.put("category", location.getCategory());
                jsonLocalization.put("description", location.getAddress());
                jsonLocalization.put("latitude", latLng.latitude);
                jsonLocalization.put("longitude", latLng.longitude);

                jsonData.put("localization", jsonLocalization);
            }

            socket.emit(Constants.createEventName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendEventExit(int eventId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("eventId", eventId);
            socket.emit(Constants.eventExitName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendEventAck(int eventId, int ack) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("eventId", eventId);
            jsonData.put("ack", ack);
            socket.emit(Constants.eventAckName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendLogout() {
        socket.emit(Constants.logoutName);
    }

    /* Methods for handling on message */
    protected void enrollOnMessageHandlers() {
        /* Add basic authentication header for connection */
         socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                // Adding headers when EVENT_REQUEST_HEADERS is called
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.v("IO", "Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers");
                        Map<String, List<String>> mHeaders = (Map<String, List<String>>)args[0];
                        mHeaders.put("Authorization", Arrays.asList("Basic bXl1c2VyOm15cGFzczEyMw=="));
                    }
                });
            }
         });

         /* Enroll event listeners */
        socket.on(Socket.EVENT_CONNECT, new OnConnectionListener());
        socket.on(Socket.EVENT_CONNECT_ERROR, new OnConnectionListener());
        socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectionListener());
        socket.on(Constants.accountRegisteredName, new AccountRegisteredListener());
        socket.on(Constants.passwordLoginName, new PasswordLoginListener());
        socket.on(Constants.sessionLoginName, new SessionLoginListener());
        socket.on(Constants.getContactListName, new GetContactListListener());
        socket.on(Constants.getPendingContactListName, new GetPendingContactListListener());
        socket.on(Constants.newPendingContactName, new NewPendingContactListener());
        socket.on(Constants.newContactName, new NewContactListener());
        socket.on(Constants.contactDeniedName, new ContactDeniedListener());
        socket.on(Constants.contactRemovedName, new ContactRemovedListener());
        socket.on(Constants.joinContactChatName, new JoinContactChatListener());
        socket.on(Constants.contactChatRemovedName, new ContactChatRemovedListener());
        socket.on(Constants.getGroupListName, new GetGroupListListener());
        socket.on(Constants.addGroupName, new AddGroupListener());
        socket.on(Constants.exitGroupName, new ExitGroupListener());
        socket.on(Constants.membersInvitedName, new MembersInvitedListener());
        socket.on(Constants.membersExitName, new MembersExitListener());
        socket.on(Constants.readMessageName, new ReadMessageListener());
        socket.on(Constants.readNbreadOfMessages, new ReadNbreadOfMessages());
        socket.on(Constants.sendMessageName, new SendMessageListener());
        socket.on(Constants.newMessageName, new NewMessageListener());
        socket.on(Constants.messageAckName, new MessageAckListener());
        socket.on(Constants.ackMessageName, new AckMessageListener());
        socket.on(Constants.getMemberJoinHistory, new GetMemberJoinHistory());
        socket.on(Constants.getEventListName, new GetEventListListener());
        socket.on(Constants.eventCreatedName, new EventCreatedListener());
        socket.on(Constants.eventExitName, new EventExitListener());
        socket.on(Constants.eventParticipantExitedName, new EventParticipantExitedListener());
        socket.on(Constants.eventAckName, new EventAckListener());
        socket.on(Constants.eventStartedName, new EventStartedListener());
    }

    /* Getter and Setters */
    public synchronized boolean isConnected() {
        return state == CONNECTED;
    }

    public synchronized void setConnected(boolean connected) {
        if (connected) {
            this.state = CONNECTED;
        } else {
            this.state = DISCONNECTED;
        }
    }
}
