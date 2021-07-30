package com.murphy.pokotalk.listener.session;

import android.content.Context;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class AccountRegisteredListener extends PokoServer.PokoListener {
    public AccountRegisteredListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.accountRegisteredName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {

    }

    @Override
    public void callError(Status status, Object... args) {

    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
