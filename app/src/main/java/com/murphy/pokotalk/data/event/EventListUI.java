package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.ListSorter;
import com.murphy.pokotalk.data.SortingList;

import java.util.Calendar;

/** Event list for UI that sorts events in event date ascending order */
public class EventListUI extends SortingList<Integer, PokoEvent> {
    @Override
    public ListSorter getListSorter() {
        return new ListSorter<Calendar, PokoEvent>(getList()) {
            @Override
            public Calendar getItemKey(PokoEvent item) {
                return item.getEventDate();
            }

            @Override
            public int compareKey(Calendar key1, Calendar key2) {
                return key1.compareTo(key2);
            }
        };
    }

    @Override
    public Integer getKey(PokoEvent pokoEvent) {
        return pokoEvent.getEventId();
    }
}
