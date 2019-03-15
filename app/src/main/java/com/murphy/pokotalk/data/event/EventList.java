package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.ItemList;

public class EventList extends ItemList<Integer, Event> {
    public EventList() {
        super();
    }

    @Override
    public Integer getKey(Event event) {
        return event.getEventId();
    }
}
