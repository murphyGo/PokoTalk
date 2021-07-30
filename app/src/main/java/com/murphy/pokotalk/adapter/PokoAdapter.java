package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;

public abstract class PokoAdapter<T> extends BaseAdapter {
    protected Context context;
    protected ViewCreationCallback<T> viewCreationCallback;
    protected HashMap<Long, View> views;

    public PokoAdapter(Context context) {
        this.context = context;
        this.views = new HashMap<>();
    }

    @Override
    public abstract int getCount();

    @Override
    public abstract T getItem(int position);

    @Override
    public abstract long getItemId(int position);

    public abstract View createView(int position, View convertView, ViewGroup parent);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent);
        putView(getItemId(position), view);

        if (viewCreationCallback != null)
            viewCreationCallback.run(view, getItem(position));
        return view;
    }

    public ViewCreationCallback getViewCreationCallback() {
        return viewCreationCallback;
    }

    public void setViewCreationCallback(ViewCreationCallback<T> viewCreationCallback) {
        this.viewCreationCallback = viewCreationCallback;
    }

    public abstract T getItemFromView(View view);

    /* Item view putter and getter implemented by HashMap */
    public void putView(long key, View value) {
        views.put(key, value);
    }

    public View getView(long key) {
        return views.get(key);
    }
}
