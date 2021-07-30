package com.murphy.pokotalk.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.murphy.pokotalk.data.db.PokoUserDatabase;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.PokoDatabaseQuery;
import com.murphy.pokotalk.data.db.json.Parser;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.PokoServer;

import java.util.Calendar;

public class Session {
    private static Session instance = null;
    private String sessionId = null;
    private Calendar sessionExpire = null;
    private Contact user = null;
    private boolean logined = false;

    public Session() {

    }

    public synchronized static Session getInstance() {
        if (instance == null)
            instance = new Session();

        return instance;
    }

    /* Returns true if the user has logined */
    public boolean hasLogined() {
        return logined;
    }

    public synchronized void setLogined() {
        logined = true;
    }

    public synchronized void setLogouted() {
        logined = false;
    }

    public synchronized boolean sessionIdExists() {
        return sessionId != null;
    }

    /* User login with this session id */
    public synchronized boolean login(Context context) {
        if (hasLogined() || sessionId == null)
            return false;

        PokoServer server = PokoServer.getInstance();
        server.sendSessionLogin(sessionId);
        return true;
    }

    public synchronized boolean logout(Context context) {
        if(!hasLogined())
            return false;

        sessionId = null;
        return true;
    }

    public synchronized void setSessionId(String id) {
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

    public static void checkSessionData() {
        int userId = getInstance().getUser().getUserId();
        PokoUserDatabase pokoDatabase = PokoUserDatabase.getInstance(null, userId);
        SQLiteDatabase db = pokoDatabase.getReadableDatabase();

        String[] selectionArgs = {Integer.toString(userId)};
        Cursor userCursor = PokoDatabaseHelper.query(db, PokoDatabaseQuery.readUserData, selectionArgs);

        try {
            if (!userCursor.moveToNext()) {
                return;
            }

            Contact user = Parser.parseUser(userCursor);
            Log.v("POKO", "USER CHECK " + user.getNickname() + "(" + user.getEmail() + "), " + user.getUserId());

            return;
        } finally {
            userCursor.close();
        }
    }
}
