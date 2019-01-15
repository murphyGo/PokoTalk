package com.murphy.pokotalk.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.Manager;
import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.listener.AccountRegisteredListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokoServer extends ServerSocket {
    private static PokoServer pokoServer;

    public PokoServer() {
        super(Constants.serverURL);
    }

    /* There is only one connection so we use singleton design */
    public static PokoServer getInstance(Context context) {
        if (pokoServer != null)
            return pokoServer;

        try {
            pokoServer = new PokoServer();
            pokoServer.createSocket(context);
            pokoServer.enrollOnMessageHandlers();
            pokoServer.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return null;
        }

        return pokoServer;
    }

    private static abstract class EventListener implements Emitter.Listener {
        private String eventName;

        @Override
        public void call(Object... args) {
            eventName = getEventName();
        }

        public abstract String getEventName();
    }

    /* Socket event listener */
    public abstract class SocketEventListener extends EventListener {
        @Override
        public void call(Object... args) {
            super.call(args);

            // Create redundant status
            Status status = new Status(Status.SUCCESS);

            call(status, args);
            PokoServer.getInstance(null).startActivityCallbacks(getEventName(), status, args);
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

                /* Start application callback */
                if (status.isSuccess())
                    callSuccess(status, args);
                else
                    callError(status, args);

                /* Start callbacks given by activities */
                PokoServer.getInstance(null).startActivityCallbacks(getEventName(), status, args);
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
        mSocket.emit("registerAccount", jsonData);
    }

    public void sendPasswordLogin(String email, String password) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", email);
        data.put("password", password);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit("passwordLogin", jsonData);
    }

    public void sendSessionLogin(String sessionId) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("sessionId", sessionId);
        JSONObject jsonData = new JSONObject(data);
        mSocket.emit("sessionLogin", jsonData);
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
        //mSocket.on(Socket.EVENT_CONNECT, onConnectListener);
        //mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener);
        //mSocket.on(Socket.EVENT_DISCONNECT, onDisconnectListener);
        mSocket.on(Constants.accountRegisteredName, new AccountRegisteredListener());
    }
}
