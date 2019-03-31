package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class EventList extends ItemPokoList<Integer, PokoEvent> {
    public EventList() {
        super();
    }

    @Override
    public Integer getKey(PokoEvent event) {
        return event.getEventId();
    }
}
