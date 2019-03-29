package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.murphy.pokotalk.data.ItemList;

import java.util.ArrayList;
import java.util.HashMap;

/* PokoTalk ListView adapter superclass for item type T
*  Activity can attach ViewCreationCallback for clicking items */
public abstract class PokoListAdapter<T> extends BaseAdapter {
    protected ArrayList<T> items;
    protected Context context;
    protected HashMap<Long, View> views;
    protected ViewCreationCallback<T> viewCreationCallback;
    protected ItemList pokoList;

    public PokoListAdapter(Context context) {
        this.context = context;
        this.views = new HashMap<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

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

    public void setViewCreationCallback(ViewCreationCallback viewCreationCallback) {
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

    public ItemList getPokoList() {
        return pokoList;
    }

    protected void setPokoList(ItemList pokoList) {
        this.pokoList = pokoList;
        this.items = pokoList.getList();
    }
}
