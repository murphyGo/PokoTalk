package com.murphy.pokotalk.adapter;

import android.content.Context;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.list.ItemPokoList;

import java.util.ArrayList;

/* PokoTalk ListView adapter superclass for item type T
*  Activity can attach ViewCreationCallback for clicking items */
public abstract class PokoListAdapter<T extends Item> extends PokoAdapter<T> {
    protected ArrayList<T> items;
    protected ItemPokoList pokoList;

    public PokoListAdapter(Context context) {
        super(context);
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

    /* Item view putter and getter implemented by HashMap */
public ItemPokoList getPokoList() {
        return pokoList;
        }

protected void setPokoList(ItemPokoList pokoList) {
        this.pokoList = pokoList;
        this.items = pokoList.getList();
        }
        }
