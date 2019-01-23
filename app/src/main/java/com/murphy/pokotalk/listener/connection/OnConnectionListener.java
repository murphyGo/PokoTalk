package com.murphy.pokotalk.listener.connection;

import com.github.nkzawa.socketio.client.Socket;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class OnConnectionListener extends PokoServer.SocketEventListener {
    @Override
    public String getEventName() {
        return Socket.EVENT_CONNECT;
    }

    @Override
    public void call(Status status, Object... args) {

    }
}
