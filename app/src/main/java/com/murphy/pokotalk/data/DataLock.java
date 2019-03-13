package com.murphy.pokotalk.data;

/** Lock for concurrency control of accessing PokoTalk data */
public class DataLock {
    private static DataLock instance = null;
    private static DataLock databaseJobInstance = null;
    protected int readNum;
    protected boolean writing;
    public static final int INTERRUPT_RETRY = 8;

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

    public static DataLock getDatabaseJobInstance() {
        if (databaseJobInstance == null) {
            databaseJobInstance = new DataLock();
        }

        return databaseJobInstance;
    }

    public synchronized void acquireReadLock() throws InterruptedException {
        int trial = 0;

        while(true) {
            try {
                while (writing) {
                    wait();
                }
                readNum++;
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                trial++;
                if (trial >= INTERRUPT_RETRY) {
                    throw e;
                }
            }
        }
    }

    public synchronized void releaseReadLock() {
        readNum--;
        notifyAll();
    }

    public synchronized void acquireWriteLock() throws InterruptedException {
        int trial = 0;

        while(true) {
            try {
                while(writing || readNum > 0) {
                    wait();
                }

                writing = true;
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                trial++;
                if (trial >= INTERRUPT_RETRY) {
                    throw e;
                }
            }
        }
    }

    public synchronized void releaseWriteLock() {
        writing = false;
        notifyAll();
    }
}