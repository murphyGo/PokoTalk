package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.extra.DateChangeItem;
import com.murphy.pokotalk.data.list.DateChangeBorderPokoList;
import com.murphy.pokotalk.data.list.ItemPokoList;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class DateChangeListAdapter<V extends Item> extends BaseAdapter {
    protected ArrayList<Item> items;
    protected Context context;
    protected HashMap<Long, View> views;
    protected ViewCreationCallback<V> viewCreationCallback;
    protected DateChangeBorderPokoList pokoList;

    public DateChangeListAdapter(Context context) {
        this.context = context;
        this.views = new HashMap<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public abstract View createView(int position, View convertView, ViewGroup parent);
    public abstract View createDateChangeView(DateChangeItem item, View convertView, ViewGroup parent);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        Item item = getItem(position);

        if (item instanceof DateChangeItem) {
            view = createDateChangeView((DateChangeItem) item, convertView, parent);

        } else if (pokoList.isInstanceof(item)) {
            view = createView(position, convertView, parent);
            putView(getItemId(position), view);

            if (viewCreationCallback != null) {
                viewCreationCallback.run(view, (V) item);
            }
        }

        return view;
    }

    public ViewCreationCallback getViewCreationCallback() {
        return viewCreationCallback;
    }

    public void setViewCreationCallback(ViewCreationCallback viewCreationCallback) {
        this.viewCreationCallback = viewCreationCallback;
    }

    public abstract V getItemFromView(View view);

    /* Item view putter and getter implemented by HashMap */
    public void putView(long key, View value) {
        views.put(key, value);
    }

    public View getView(long key) {
        return views.get(key);
    }

    public ItemPokoList getPokoList() {
        return pokoList;
    }

    protected void setPokoList(DateChangeBorderPokoList pokoList) {
        this.pokoList = pokoList;
        this.items = pokoList.getListForAdapter();
    }
}
