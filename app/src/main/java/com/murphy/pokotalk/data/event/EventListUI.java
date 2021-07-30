package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.list.DateChangeBorderPokoList;
import com.murphy.pokotalk.data.list.ListSorter;

import java.util.Calendar;

/** Event list for UI that sorts events in event date ascending order */
public class EventListUI extends DateChangeBorderPokoList<Integer, Calendar, PokoEvent> {
    @Override
    public ListSorter getListSorter() {
        return new ListSorter<Calendar, PokoEvent>(getList()) {
            @Override
            public Calendar getItemKey(PokoEvent item) {
                return item.getEventDate();
            }

            @Override
            public int compareKey(Calendar key1, Calendar key2) {
                return key2.compareTo(key1);
            }
        };
    }

    @Override
    protected Calendar getTimeKey(PokoEvent item) {
        return (Calendar) item.getEventDate().clone();
    }

    @Override
    public Integer getKey(PokoEvent pokoEvent) {
        return pokoEvent.getEventId();
    }

    @Override
    protected Calendar getDate(PokoEvent item) {
        return (Calendar) item.getEventDate().clone();
    }

    @Override
    protected int compareTwoKey(Calendar key1, Calendar key2) {
        return key2.compareTo(key1);
    }

    @Override
    public boolean isInstanceof(Item item) {
        return item instanceof PokoEvent;
    }
}
