package com.murphy.pokotalk.adapter;

import android.view.View;

public abstract class ViewCreationCallback<T> implements Runnable {
    @Override
    public void run() {

    }

    public abstract void run(View view, T item);
}
