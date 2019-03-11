package com.murphy.pokotalk.data.file;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.file.json.Reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class PokoSequencialAccessFile<T> extends PokoMultiItemsFile<T> {
    protected FileInputStream fileInputStream;
    protected InputStreamReader inputStreamReader;
    protected BufferedReader bufferedReader;
    protected FileOutputStream fileOutputStream;
    protected OutputStreamWriter outputStreamWriter;

    public void openReader() throws IOException {
        Log.v("POKO", "Open reader " + getFullFilePath());
        makeSureFullDirectoryExists();
        fileInputStream = new FileInputStream(getFullFilePath());
        inputStreamReader = new InputStreamReader(fileInputStream, Constants.fileEncoding);
        bufferedReader = new BufferedReader(inputStreamReader);
        jsonReader = new Reader() {
            @Override
            public int readChars(char[] buffer, int offset, int size) throws IOException {
                return bufferedReader.read(buffer, offset, size);
            }
        };
    }

    public void openWriter(boolean append) throws IOException {
        Log.v("POKO", "Open writer " + getFullFilePath());
        makeSureFullDirectoryExists();
        fileOutputStream = new FileOutputStream(getFullFilePath(), append);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream, Constants.fileEncoding);
    }

    public void closeReader() throws IOException {
        Log.v("POKO", "close reader");
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
        bufferedReader = null;
        inputStreamReader = null;
        fileOutputStream = null;
    }

    public void closeWriter() throws IOException {
        Log.v("POKO", "Close writer");
        outputStreamWriter.close();
        fileOutputStream.close();
        outputStreamWriter = null;
        fileOutputStream = null;
    }

    public void flush() throws IOException {
        outputStreamWriter.flush();
    }
}
