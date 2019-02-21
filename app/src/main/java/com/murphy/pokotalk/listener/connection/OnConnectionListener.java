package com.murphy.pokotalk.listener.connection;

import com.github.nkzawa.socketio.client.Socket;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class OnConnectionListener extends PokoServer.SocketEventListener {
    @Override
    public String getEventName() {
        return Socket.EVENT_CONNECT;
    }

    @Override
    public void call(Status status, Object... args) {
        PokoServer.getInstance(null).setConnected(true);

        /* If session id exists and the user has not logined, try session login */
        Session session = Session.getInstance();
        String sessionId = session.getSessionId();
        if (sessionId != null && !session.hasLogined()) {
            PokoServer.getInstance(null).sendSessionLogin(sessionId);
        }
    }
}
