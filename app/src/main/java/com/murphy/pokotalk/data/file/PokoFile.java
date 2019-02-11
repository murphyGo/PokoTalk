package com.murphy.pokotalk.data.file;

import android.util.Log;

import com.murphy.pokotalk.data.file.json.Reader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;

public abstract class PokoFile<T> {
    protected FileInputStream fileInputStream;
    protected InputStreamReader inputStreamReader;
    protected BufferedReader bufferedReader;
    protected FileOutputStream fileOutputStream;
    protected OutputStreamWriter outputStreamWriter;
    protected String fullFilePath;
    protected String fullDirectoryPath;
    protected ArrayDeque<String> tokenDeque;
    protected Reader jsonReader;

    public PokoFile() {
        fullFilePath = null;
        tokenDeque = new ArrayDeque<String>();
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

    public void openReader() throws IOException {
        Log.v("POKO", "Open reader");
        makeSureFullDirectoryExists();
        fileInputStream = new FileInputStream(getFullFilePath());
        inputStreamReader = new InputStreamReader(fileInputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
        jsonReader = new Reader(bufferedReader);
    }

    public void openWriter() throws IOException {
        Log.v("POKO", "Open writer");
        makeSureFullDirectoryExists();
        fileOutputStream = new FileOutputStream(getFullFilePath());
        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    }

    public void closeReader() throws IOException {
        Log.v("POKO", "close reader");
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
    }

    public void closeWriter() throws IOException {
        Log.v("POKO", "Close writer");
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public void flush() throws IOException {
        outputStreamWriter.flush();
    }

    public abstract void save() throws IOException, JSONException;
    public abstract T read() throws IOException, JSONException;

    /* Reads one json object from input */
    public JSONObject readJSON() throws IOException, JSONException {
        JSONObject jsonObject = jsonReader.readJSON();
        Log.v("POKO", "READ JSON " + jsonObject.toString());
        return jsonObject;
    }
}
