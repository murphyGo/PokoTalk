package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.ItemList;

public class EventList extends ItemList<Integer, PokoEvent> {
    public EventList() {
        super();
    }

    @Override
    public Integer getKey(PokoEvent event) {
        return event.getEventId();
    }
}
