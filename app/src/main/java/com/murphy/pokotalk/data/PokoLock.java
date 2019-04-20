package com.murphy.pokotalk.data;

/** Lock for concurrency control of accessing PokoTalk data */
public class PokoLock {
    private static PokoLock instance = null;
    private static PokoLock databaseJobInstance = null;
    protected int readNum;
    protected boolean writing;
    public static final int INTERRUPT_RETRY = 8;

    public PokoLock() {
        readNum = 0;
        writing = false;
    }

    public static PokoLock getDataLockInstance() {
        if (instance == null) {
            synchronized (PokoLock.class) {
                instance = instance == null ? new PokoLock() : instance;
            }
        }

        return instance;
    }

    public static PokoLock getDatabaseJobInstance() {
        if (databaseJobInstance == null) {
            databaseJobInstance = new PokoLock();
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