package com.murphy.pokotalk.listener.connection;

import android.content.Context;

import com.github.nkzawa.socketio.client.Socket;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class OnConnectionListener extends PokoServer.SocketEventListener {
    public OnConnectionListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Socket.EVENT_CONNECT;
    }

    @Override
    public void call(Status status, Object... args) {
        PokoServer.getInstance().setConnected(true);

        /* If session id exists and the user has not signed in, try session login */
        Session session = Session.getInstance();
        String sessionId = session.getSessionId();
        if (sessionId != null && !session.hasLogined()) {
            PokoServer.getInstance().sendSessionLogin(sessionId);
        }
    }
}
