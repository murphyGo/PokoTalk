package com.murphy.pokotalk.data.file;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SessionFile extends PokoFile<Session> {
    protected Session session;

    @Override
    public String getRestPath() {
        return ".";
    }

    @Override
    public String getFileName() {
        return Constants.sessionFile;
    }

    @Override
    public Session read() throws IOException, JSONException{
        JSONObject jsonSession = readJSON();
        if (jsonSession == null) {
            return null;
        }

        Session session = Parser.parseSessionJSON(jsonSession);
        Log.v("POKO", "Open session");
        return session;
    }

    @Override
    public void save() throws IOException, JSONException {
        session = Session.getInstance();
        JSONObject jsonSession = Serializer.makeSessionJSON(session);
        outputStreamWriter.write(jsonSession.toString());
        Log.v("POKO", "Save session");
    }
}
