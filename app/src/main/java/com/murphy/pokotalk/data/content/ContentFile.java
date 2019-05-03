package com.murphy.pokotalk.data.content;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class ContentFile {
    protected String fullFilePath;
    protected String fullDirectoryPath;
    protected String fileName;

    protected File file;
    protected FileInputStream fileInputStream;
    protected FileOutputStream fileOutputStream;

    public ContentFile() {
        fullFilePath = null;
    }

    public abstract String getRootPath();

    public abstract String getRestPath();

    public String getFullFilePath() {
        if (fullFilePath == null) {
            fullFilePath = getFullDirectoryPath() + File.separator + getFileName();
        }
        return fullFilePath;
    }

    public String getFullDirectoryPath() {
        if (fullDirectoryPath == null) {
            fullDirectoryPath = getRootPath() + File.separator
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
        Log.v("POKO", "Open reader " + getFullFilePath());
        if (file == null) {
            file = new File(getFullFilePath());
        }
        fileInputStream = new FileInputStream(file);
    }

    public void openWriter(boolean append) throws IOException {
        Log.v("POKO", "Open writer " + getFullFilePath());
        makeSureFullDirectoryExists();
        if (file == null) {
            file = new File(getFullFilePath());
        }
        fileOutputStream = new FileOutputStream(file, append);
    }

    public void closeReader() throws IOException {
        Log.v("POKO", "close reader");
        fileInputStream.close();
        fileOutputStream = null;
    }

    public void closeWriter() throws IOException {
        Log.v("POKO", "Close writer");
        fileOutputStream.close();
        fileOutputStream = null;
    }

    public void flush() throws IOException {
        fileOutputStream.flush();
    }

    public void save(byte[] bytes) throws IOException {
        fileOutputStream.write(bytes);
    }

    public byte[] read() throws IOException {
        // Get file length
        long size = file.length();

        // Check if file size does not fit in signed integer
        if (size > (long) ((1 << 31) - 1)) {
            throw new IOException("File size too big");
        }

        byte[] buffer = new byte[(int) size];

        fileInputStream.read(buffer, 0, (int) size);

        return buffer;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
