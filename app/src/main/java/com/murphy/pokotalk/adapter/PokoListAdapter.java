package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/* PokoTalk ListView adapter superclass for item type T
*  Activity can attach ViewCreationCallback for clicking items */
public abstract class PokoListAdapter<T> extends BaseAdapter {
    protected ArrayList<T> items;
    protected Context context;
    protected ViewCreationCallback viewCreationCallback;

    public PokoListAdapter(Context context, ArrayList<T> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public abstract long getItemId(int position);

    public abstract View createView(int position, View convertView, ViewGroup parent);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent);
        viewCreationCallback.run(view);
        return view;
    }

    public ViewCreationCallback getViewCreationCallback() {
        return viewCreationCallback;
    }

    public void setViewCreationCallback(ViewCreationCallback viewCreationCallback) {
        this.viewCreationCallback = viewCreationCallback;
    }

    public abstract T getItemFromView(View view);
}
