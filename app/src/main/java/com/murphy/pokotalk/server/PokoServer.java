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
import com.murphy.pokotalk.data.PokoLock;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.data.db.PokoDatabaseManager;
import com.murphy.pokotalk.data.event.EventLocation;
import com.murphy.pokotalk.listener.chat.AckMessageListener;
import com.murphy.pokotalk.listener.chat.GetMemberJoinHistory;
import com.murphy.pokotalk.listener.chat.MessageAckListener;
import com.murphy.pokotalk.listener.chat.NewMessageListener;
import com.murphy.pokotalk.listener.chat.ReadMessageListener;
import com.murphy.pokotalk.listener.chat.ReadNbreadOfMessages;
import com.murphy.pokotalk.listener.chat.SendFileShareMessageListener;
import com.murphy.pokotalk.listener.chat.SendImageMessageListener;
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
import com.murphy.pokotalk.listener.content.DownloadListener;
import com.murphy.pokotalk.listener.content.StartDownloadListener;
import com.murphy.pokotalk.listener.content.StartUploadListener;
import com.murphy.pokotalk.listener.content.UploadListener;
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
import com.murphy.pokotalk.listener.locationShare.ExitRealtimeLocationShareListener;
import com.murphy.pokotalk.listener.locationShare.JoinRealtimeLocationShareListener;
import com.murphy.pokotalk.listener.locationShare.RealtimeLocationShareBroadcastListener;
import com.murphy.pokotalk.listener.locationShare.UpdateRealtimeLocationListener;
import com.murphy.pokotalk.listener.session.AccountRegisteredListener;
import com.murphy.pokotalk.listener.session.PasswordLoginListener;
import com.murphy.pokotalk.listener.session.SessionLoginListener;
import com.murphy.pokotalk.listener.setting.UpdateProfileImageListener;
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
    protected Context context;

    public static final int IDLE = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 3;
    
    public PokoServer() {
        super(Constants.serverURL);
    }

    /* There is only one connection so we use singleton design
    * Socket try to connect only when getDataLockInstance method is called with context not null */
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
                this.context = context;
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
        protected String eventName;
        protected Context context;

        public EventListener(Context context) {
            data = new HashMap<>();
            this.context = context;
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
        public SocketEventListener(Context context) {
            super(context);
        }
        
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
        public PokoListener(Context context) {
            super(context);
        }
        
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

                // Clear data
                this.data.clear();

                // Put context data
                this.data.put("context", context);

                PokoLock.getDataLockInstance().acquireWriteLock();
                try {
                    Log.v("SERVER DATA " + getEventName(), data.toString());
                    //Log.v("HANDLING THREAD " + getEventName(), "" + Thread.currentThread().getId());
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
                    PokoLock.getDataLockInstance().releaseWriteLock();
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

    public void sendNewImageMessage(int groupId, int messageSendId, int imageSendId,
                               @Nullable Integer importanceLevel) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("messageSendId", messageSendId);
            jsonData.put("imageSendId", imageSendId);
            jsonData.put("importance", importanceLevel);
            socket.emit(Constants.sendImageMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNewFileShareMessage(int groupId, int messageSendId, int fileSendId,
                                    @Nullable Integer importanceLevel, String fileName) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("messageSendId", messageSendId);
            jsonData.put("fileSendId", fileSendId);
            jsonData.put("importance", importanceLevel);
            jsonData.put("fileName", fileName);
            socket.emit(Constants.sendFileShareMessageName, jsonData);
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

    public void sendUpdateProfileImage(int sendId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("sendId", sendId);
            socket.emit(Constants.updateProfileImageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendStartUpload(int id, long size, String extension) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("uploadId", id);
            jsonData.put("size", size);
            jsonData.put("extension", extension);
            socket.emit(Constants.startUploadName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendUpload(int id, byte[] binary) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("uploadId", id);
            jsonData.put("buf", binary);
            socket.emit(Constants.uploadName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendStartDownload(String contentName, String type, int sendId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("contentName", contentName);
            jsonData.put("type", type);
            jsonData.put("sendId", sendId);
            socket.emit(Constants.startDownloadName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendDownloadAck(int downloadId, int ack) {
        try {
            Log.v("POKO", "DOWNLOA ACK " + ack);
            JSONObject jsonData = new JSONObject();
            jsonData.put("downloadId", downloadId);
            jsonData.put("ack", ack);
            socket.emit(Constants.downloadAckName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendJoinRealtimeLocationShare(int eventId, Integer number) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("eventId", eventId);
            if (number != null) {
                jsonData.put("number", number);
            }
            socket.emit(Constants.joinRealtimeLocationShareName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendExitRealtimeLocationShare(int eventId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("eventId", eventId);
            socket.emit(Constants.exitRealtimeLocationShareName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateRealtimeLocation(int eventId, double lat, double lng) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("eventId", eventId);
            jsonData.put("lat", lat);
            jsonData.put("lng", lng);
            socket.emit(Constants.updateRealtimeLocationName, jsonData);
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
        socket.on(Socket.EVENT_CONNECT, new OnConnectionListener(context));
        socket.on(Socket.EVENT_CONNECT_ERROR, new OnConnectionListener(context));
        socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectionListener(context));
        socket.on(Constants.accountRegisteredName, new AccountRegisteredListener(context));
        socket.on(Constants.passwordLoginName, new PasswordLoginListener(context));
        socket.on(Constants.sessionLoginName, new SessionLoginListener(context));
        socket.on(Constants.getContactListName, new GetContactListListener(context));
        socket.on(Constants.getPendingContactListName, new GetPendingContactListListener(context));
        socket.on(Constants.newPendingContactName, new NewPendingContactListener(context));
        socket.on(Constants.newContactName, new NewContactListener(context));
        socket.on(Constants.contactDeniedName, new ContactDeniedListener(context));
        socket.on(Constants.contactRemovedName, new ContactRemovedListener(context));
        socket.on(Constants.joinContactChatName, new JoinContactChatListener(context));
        socket.on(Constants.contactChatRemovedName, new ContactChatRemovedListener(context));
        socket.on(Constants.getGroupListName, new GetGroupListListener(context));
        socket.on(Constants.addGroupName, new AddGroupListener(context));
        socket.on(Constants.exitGroupName, new ExitGroupListener(context));
        socket.on(Constants.membersInvitedName, new MembersInvitedListener(context));
        socket.on(Constants.membersExitName, new MembersExitListener(context));
        socket.on(Constants.readMessageName, new ReadMessageListener(context));
        socket.on(Constants.readNbreadOfMessages, new ReadNbreadOfMessages(context));
        socket.on(Constants.sendMessageName, new SendMessageListener(context));
        socket.on(Constants.sendImageMessageName, new SendImageMessageListener(context));
        socket.on(Constants.sendFileShareMessageName, new SendFileShareMessageListener(context));
        socket.on(Constants.newMessageName, new NewMessageListener(context));
        socket.on(Constants.messageAckName, new MessageAckListener(context));
        socket.on(Constants.ackMessageName, new AckMessageListener(context));
        socket.on(Constants.getMemberJoinHistory, new GetMemberJoinHistory(context));
        socket.on(Constants.getEventListName, new GetEventListListener(context));
        socket.on(Constants.eventCreatedName, new EventCreatedListener(context));
        socket.on(Constants.eventExitName, new EventExitListener(context));
        socket.on(Constants.eventParticipantExitedName, new EventParticipantExitedListener(context));
        socket.on(Constants.eventAckName, new EventAckListener(context));
        socket.on(Constants.eventStartedName, new EventStartedListener(context));
        socket.on(Constants.startUploadName, new StartUploadListener(context));
        socket.on(Constants.uploadName, new UploadListener(context));
        socket.on(Constants.startDownloadName, new StartDownloadListener(context));
        socket.on(Constants.downloadName, new DownloadListener(context));
        socket.on(Constants.updateProfileImageName, new UpdateProfileImageListener(context));
        socket.on(Constants.joinRealtimeLocationShareName, new JoinRealtimeLocationShareListener(context));
        socket.on(Constants.exitRealtimeLocationShareName, new ExitRealtimeLocationShareListener(context));
        socket.on(Constants.updateRealtimeLocationName, new UpdateRealtimeLocationListener(context));
        socket.on(Constants.realtimeLocationShareBroadcastName, new RealtimeLocationShareBroadcastListener(context));
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
