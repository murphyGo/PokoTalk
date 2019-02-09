package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.util.Log;
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
    protected ViewCreationCallback viewCreationCallback;
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
    public Object getItem(int position) {
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
            viewCreationCallback.run(view);
        return view;
    }

    public ViewCreationCallback getViewCreationCallback() {
        return viewCreationCallback;
    }

    public void setViewCreationCallback(ViewCreationCallback viewCreationCallback) {
        this.viewCreationCallback = viewCreationCallback;
    }

    public abstract void refreshView(View view, T item);

    /* Refreshes view for the item */
    public boolean refreshView(T item) {
        int index = items.indexOf(item);
        Log.v("refresh view index", index + "");
        if (index < 0)
            return false;
        View view = views.get(getItemId(index));
        Log.v("refresh view view", view == null ? "null" : view.toString());
        refreshView(view, item);
        return true;
    }

    public void refreshAllExistingViews() {
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            View view = views.get(getItemId(i));
            if (view != null)
                refreshView(view, item);
            notifyDataSetChanged();
        }
    }

    /* Get item view by item id(returned from getItemId method) */
    public View findViewByItemId(long id) {
        return views.get(id);
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
