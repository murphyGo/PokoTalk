package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.ItemList;

public class EventList extends ItemList<Integer, Event> {
    public EventList() {
        super();
    }

    @Override
    public void updateItem(Event item) {
        super.updateItem(item);
    }

    @Override
    public Integer getKey(Event event) {
        return event.getEventId();
    }


}
