package com.murphy.pokotalk.listener;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class PasswordLoginListener extends PokoServer.PokoListener {
    @Override
    public String getEventName() {
        return Constants.passwordLoginName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {

    }

    @Override
    public void callError(Status status, Object... args) {

    }
}
