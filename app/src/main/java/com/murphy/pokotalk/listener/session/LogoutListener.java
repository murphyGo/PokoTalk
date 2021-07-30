package com.murphy.pokotalk.listener.session;

import android.content.Context;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoAsyncDatabaseJob;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

public class LogoutListener extends PokoServer.PokoListener {
    public LogoutListener(Context context) {
        super(context);
    }

    @Override
    public String getEventName() {
        return Constants.logoutName;
    }

    @Override
    public void callSuccess(Status status, Object... args) {
        /* Set session logoutState and remove session data */
        Session session = Session.getInstance();
        if (session.hasLogined()) {
            session.setLogouted();

            //TODO: Remove session id file
        }
    }

    @Override
    public void callError(Status status, Object... args) {

    }

    @Override
    public PokoAsyncDatabaseJob getDatabaseJob() {
        return null;
    }
}
