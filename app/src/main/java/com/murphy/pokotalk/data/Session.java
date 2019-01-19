package com.murphy.pokotalk.data;

import android.content.Context;

import com.murphy.pokotalk.server.PokoServer;

import java.util.Calendar;

public class Session {
    private static Session instance;
    private String sessionId;
    private Calendar sessionExpire;
    private Contact user;
    private boolean logined;

    public Session() {
        sessionId = null;
        logined = false;
    }

    public static Session getInstance() {
        if (instance == null)
            instance = new Session();

        return instance;
    }

    /* Returns true if the user has logined */
    public boolean hasLogined() {
        return logined;
    }

    public void setLogined() {
        logined = true;
    }

    public void setLogouted() {
        logined = false;
    }

    public boolean sessionIdExists() {
        return sessionId != null;
    }

    /* User login with this session id */
    public boolean login(Context context) {
        if (hasLogined() || sessionId == null)
            return false;

        PokoServer server = PokoServer.getInstance(context);
        server.sendSessionLogin(sessionId);
        return true;
    }

    public boolean logout(Context context) {
        if(!hasLogined())
            return false;

        PokoServer.getInstance(context);
        sessionId = null;
        return true;
    }

    public void setSessionId(String id) {
        sessionId = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionExpire(Calendar expireDate) {
        sessionExpire = expireDate;
    }

    public Calendar getSessionExpire() {
        return sessionExpire;
    }

    public void setUser(Contact user) {
        this.user = user;
    }

    public Contact getUser() {
        return user;
    }

    public static void reset() {
        instance = null;
    }
}
