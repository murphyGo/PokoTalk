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
import com.murphy.pokotalk.listener.chat.AckMessageListener;
import com.murphy.pokotalk.listener.chat.MessageAckListener;
import com.murphy.pokotalk.listener.chat.NewMessageListener;
import com.murphy.pokotalk.listener.chat.ReadMessageListener;
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
import com.murphy.pokotalk.listener.group.AddGroupListener;
import com.murphy.pokotalk.listener.group.ExitGroupListener;
import com.murphy.pokotalk.listener.group.GetGroupListListener;
import com.murphy.pokotalk.listener.group.MembersExitListener;
import com.murphy.pokotalk.listener.group.MembersInvitedListener;
import com.murphy.pokotalk.listener.session.AccountRegisteredListener;
import com.murphy.pokotalk.listener.session.PasswordLoginListener;
import com.murphy.pokotalk.listener.session.SessionLoginListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokoServer extends ServerSocket {
    protected static PokoServer pokoServer = null;
    protected boolean connecting;
    protected boolean connected;

    public PokoServer() {
        super(Constants.serverURL);
        connecting = false;
    }

    /* There is only one connection so we use singleton design
    * Socket try to connect only when getInstance method is called with context not null */
    public static PokoServer getInstance(@Nullable Context context) {
        try {
            if (pokoServer == null) {
                pokoServer = new PokoServer();
            }
            if (!pokoServer.connecting && context != null) {
                pokoServer.createSocket(context);
                pokoServer.enrollOnMessageHandlers();
                pokoServer.connect();
                pokoServer.connecting = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Socket error", e.toString());
            return null;
        }

        return pokoServer;
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
            PokoServer.getInstance(null).startActivityCallbacks(getEventName(), status, data, args);
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

                Log.v("SERVER DATA " + getEventName(), data.toString());
                /* Start application callback */
                if (status.isSuccess()) {
                    callSuccess(status, args);
                } else {
                    callError(status, args);
                }

                /* Start callbacks given by activities */
                PokoServer.getInstance(null).
                        startActivityCallbacks(getEventName(), status, this.data, args);
            } catch(JSONException e) {
                Log.e("Poko", "Bad json status data");
                return;
            }
        }

        public abstract void callSuccess(Status status, Object... args);
        public abstract void callError(Status status, Object... args);
    }

    /* Methods for message send */
    public void sendAccountRegister(String name, String email, String password) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.accountRegisteredName, jsonData);
    }

    public void sendPasswordLogin(String email, String password) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        data.put("password", password);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.passwordLoginName, jsonData);
    }

    public void sendSessionLogin(String sessionId) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("sessionId", sessionId);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.sessionLoginName, jsonData);
    }

    public void sendGetContactList() {
        mSocket.emit(Constants.getContactListName);
    }

    public void sendGetPendingContactList() {
        mSocket.emit(Constants.getPendingContactListName);
    }

    public void sendAddContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.addContactName, jsonData);
    }

    public void sendRemoveContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.removeContactName, jsonData);
    }

    public void sendAcceptContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.acceptContactName, jsonData);
    }

    public void sendDenyContact(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.denyContactName, jsonData);
    }

    public void sendJoinContactChat(String email) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit(Constants.joinContactChatName, jsonData);
    }

    public void sendGetGroupList() {
        mSocket.emit(Constants.getGroupListName);
    }

    public void sendAddGroup(String name, ArrayList<String> emails) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", name);
            JSONArray jsonEmailArray = new JSONArray(emails);
            jsonData.put("members", jsonEmailArray);
            mSocket.emit(Constants.addGroupName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendExitGroup(int groupId) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            mSocket.emit(Constants.exitGroupName, jsonData);
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
            mSocket.emit(Constants.sendMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendReadMessage(int groupId, int nbMessageMax) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            jsonData.put("nbMessageMax", nbMessageMax);
            mSocket.emit(Constants.readMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendInviteGroupMembers(int groupId, ArrayList<String> emails) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("groupId", groupId);
            JSONArray jsonEmailArray = new JSONArray(emails);
            jsonData.put("members", jsonEmailArray);
            mSocket.emit(Constants.inviteGroupMembersName, jsonData);
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
            mSocket.emit(Constants.ackMessageName, jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Methods for handling on message */
    protected void enrollOnMessageHandlers() {
        /* Add basic authentication header for connection */
         mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
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
        mSocket.on(Socket.EVENT_CONNECT, new OnConnectionListener());
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new OnConnectionListener());
        mSocket.on(Socket.EVENT_DISCONNECT, new OnDisconnectionListener());
        mSocket.on(Constants.accountRegisteredName, new AccountRegisteredListener());
        mSocket.on(Constants.passwordLoginName, new PasswordLoginListener());
        mSocket.on(Constants.sessionLoginName, new SessionLoginListener());
        mSocket.on(Constants.getContactListName, new GetContactListListener());
        mSocket.on(Constants.getPendingContactListName, new GetPendingContactListListener());
        mSocket.on(Constants.newPendingContactName, new NewPendingContactListener());
        mSocket.on(Constants.newContactName, new NewContactListener());
        mSocket.on(Constants.contactDeniedName, new ContactDeniedListener());
        mSocket.on(Constants.contactRemovedName, new ContactRemovedListener());
        mSocket.on(Constants.joinContactChatName, new JoinContactChatListener());
        mSocket.on(Constants.contactChatRemovedName, new ContactChatRemovedListener());
        mSocket.on(Constants.getGroupListName, new GetGroupListListener());
        mSocket.on(Constants.addGroupName, new AddGroupListener());
        mSocket.on(Constants.exitGroupName, new ExitGroupListener());
        mSocket.on(Constants.membersInvitedName, new MembersInvitedListener());
        mSocket.on(Constants.membersExitName, new MembersExitListener());
        mSocket.on(Constants.readMessageName, new ReadMessageListener());
        mSocket.on(Constants.sendMessageName, new SendMessageListener());
        mSocket.on(Constants.newMessageName, new NewMessageListener());
        mSocket.on(Constants.messageAckName, new MessageAckListener());
        mSocket.on(Constants.ackMessageName, new AckMessageListener());
    }

    /* Getter and Setters */
    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }
}
