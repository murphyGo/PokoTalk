package com.murphy.pokotalk;

public class Session {
    private static Session session;

    private String sessionId;

    public static Session getInstance() {
        if (session == null)
            session = new Session();

        return session;
    }

    /* Returns true if the user is logined */
    public boolean hasLogined() {
        return sessionId != null;
    }

    /* User login with this session id */
    public boolean login(String sId) {
        if (sessionId != null)
            return false;

        sessionId = sId;
        return true;
    }

    public boolean logout() {
        if(sessionId == null)
            return false;

        sessionId = null;
        return true;
    }

}
