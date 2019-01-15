package com.murphy.pokotalk.server;

public abstract class ActivityCallback implements Runnable {
    private Object[] args;
    private Status status;

    public void setArgs(final Status status, final Object... args) {
        this.args = args;
        this.status = status;
    }

    @Override
    public void run() {
        if (status.isSuccess())
            onSuccess(status, args);
        else
            onError(status, args);
    }

    public abstract void onSuccess(Status status, final Object... args);
    public abstract void onError(Status status, final Object... args);
}
