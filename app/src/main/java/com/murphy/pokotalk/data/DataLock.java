package com.murphy.pokotalk.data;

import android.util.Log;

/** Lock for concurrency control of accessing PokoTalk data */
public class DataLock {
    private static DataLock instance = null;
    int readNum;
    boolean writing;

    public DataLock() {
        readNum = 0;
        writing = false;
    }

    public static DataLock getInstance() {
        if (instance == null) {
            instance = new DataLock();
        }

        return instance;
    }

    public synchronized void acquireReadLock() throws InterruptedException {
        while (writing) {
            wait();
        }
        readNum++;
    }

    public synchronized void releaseReadLock() {
        readNum--;
        notifyAll();
    }

    public synchronized void acquireWriteLock() throws InterruptedException {
        while(writing || readNum > 0) {
            wait();
        }
        Log.v("POKO", "ACQUIRE WRITE LOCK");
        writing = true;
    }

    public synchronized void releaseWriteLock() {
        writing = false;
        Log.v("POKO", "RELEASE WRITE LOCK");
        notifyAll();
    }
}