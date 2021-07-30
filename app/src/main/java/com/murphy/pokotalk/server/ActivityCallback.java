package com.murphy.pokotalk.server;

import java.util.HashMap;

/* Server socket event listener for activity side.
   More than 1 activity callbacks can be attached to a socket event.
   If so, every attached activity callbacks are called from first to the last.
   Activity callbacks are always called after counterpart application listener
   in com.murphy.pokotalk.listener package.
 */
public abstract class ActivityCallback implements Runnable {
    private Object[] args;
    private HashMap<String, Object> data;
    private Status status;

    public void setArgs(final Status status,
                        final HashMap<String, Object> data, final Object... args) {
        this.args = args;
        this.status = status;
        this.data = data;
    }

    @Override
    public void run() {
        if (status.isSuccess())
            onSuccess(status, args);
        else
            onError(status, args);
    }

    public void putData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public abstract void onSuccess(Status status, final Object... args);
    public abstract void onError(Status status, final Object... args);
}
