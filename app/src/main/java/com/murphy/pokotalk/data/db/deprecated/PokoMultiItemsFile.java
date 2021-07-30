package com.murphy.pokotalk.data.db.deprecated;

import com.murphy.pokotalk.data.Session;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/** Extends PokoFile with multiple items feature */
public abstract class PokoMultiItemsFile<T> extends PokoFile<T> {
    protected ArrayList<T> dataList;

    public PokoMultiItemsFile() {
        super();
    }

    @Override
    public String getRestPath() {
        return Integer.toString(Session.getInstance().getUser().getUserId());
    }

    public abstract int getItemListSize();

    public abstract void addItem(T item);

    public abstract T getItemAt(int position);

    public abstract void saveItem(T item) throws IOException, JSONException;

    public void preSaveItem(T item) throws IOException, JSONException {

    }

    public void postSaveItem(T item) throws IOException, JSONException {

    }

    /** Saves all items in list to file */
    @Override
    public void save() throws IOException, JSONException {
        int size = getItemListSize();
        for (int i = 0; i < size; i++) {
            T item = getItemAt(i);
            if (item != null) {
                saveItemProcess(item);
            }
        }
    }

    /** Reads all items from file */
    public void readAll() throws IOException, JSONException {
        T item;
        while((item = read()) != null) {
            addItem(item);
        }
    }

    public void saveItemProcess(T item) throws IOException, JSONException {
        preSaveItem(item);
        saveItem(item);
        postSaveItem(item);
    }
}
