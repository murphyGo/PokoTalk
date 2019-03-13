package com.murphy.pokotalk.data.file.deprecated;

import android.util.Log;

import com.murphy.pokotalk.data.file.json.Reader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;

public abstract class PokoFile<T> {
    protected String fullFilePath;
    protected String fullDirectoryPath;
    protected ArrayDeque<String> tokenDeque;
    protected Reader jsonReader;

    public PokoFile() {
        fullFilePath = null;
        tokenDeque = new ArrayDeque<>();
    }

    public abstract String getFileName();

    public abstract String getRestPath();

    public String getFullFilePath() {
        if (fullFilePath == null) {
            fullFilePath = getFullDirectoryPath() + File.separator + getFileName();
        }
        return fullFilePath;
    }

    public String getFullDirectoryPath() {
        if (fullDirectoryPath == null) {
            fullDirectoryPath = FileManager.getRootPath() + File.separator
                    + getRestPath();
        }
        return fullDirectoryPath;
    }

    public void makeSureFullDirectoryExists() throws IOException {
        File directory = new File(getFullDirectoryPath());
        if (!directory.exists()) {
            Log.v("POKO", "Creates directories");
            if (!directory.mkdirs())
                throw new IOException("Failed to create directories");
        }
    }

    public abstract void flush() throws IOException;

    public abstract void save() throws IOException, JSONException;
    public abstract T read() throws IOException, JSONException;

    /* Reads one json object from input */
    public JSONObject readJSON() throws IOException, JSONException {
        JSONObject jsonObject = jsonReader.readJSON();
        //if (jsonObject != null)
        //    Log.v("POKO", "READ JSON " + jsonObject.toString());
        return jsonObject;
    }
}
