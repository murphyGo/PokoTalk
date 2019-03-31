package com.murphy.pokotalk.data.extra;

import com.murphy.pokotalk.data.Item;

import java.util.Calendar;

public class ExtraItem<K> extends Item {
    protected K key;
    protected Calendar date;

    public ExtraItem() {
        setSurviveOnListUpdate(true);
    }

    @Override
    public void update(Item item) {

    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
