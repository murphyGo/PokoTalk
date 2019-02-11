package com.murphy.pokotalk.data.file;

import android.os.Environment;
import android.util.Log;

import com.murphy.pokotalk.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/* Manages saving data as a file, reading from and writing to files */
public class FileManager {
    protected static FileManager instance;
    protected static String rootPath = null;

    public FileManager() {

    }

    public static FileManager getInstance() {
        if (instance == null)
            instance = new FileManager();

        return instance;
    }

    public static String getRootPath() {
        if (rootPath == null) {
            File sdcardFolder = Environment.getExternalStorageDirectory();
            String sdcardPath = sdcardFolder.getAbsolutePath();
            rootPath = sdcardPath + File.separator + Constants.rootDirectory;
        }

        Log.v("ROOT PATH", rootPath);
        return rootPath;
    }

    public void makeSureRootDirectoryExists() throws FileNotFoundException, IOException {
        /* Make sure the root directory exists */
        File directory = new File(getRootPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public boolean saveSession() {
        try {
            SessionFile sessionFile = new SessionFile();
            sessionFile.openWriter();
            sessionFile.save();
            sessionFile.flush();
            sessionFile.closeWriter();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean loadSession() {
        try {
            SessionFile sessionFile = new SessionFile();
            sessionFile.openReader();
            sessionFile.read();
            sessionFile.closeReader();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
