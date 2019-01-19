package com.murphy.pokotalk.data;

import java.util.ArrayList;

public class EventList extends List<Integer, Event> {
    public EventList() {
        super();
    }

    @Override
    public Integer getKey(Event event) {
        return event.getEventId();
    }

    public boolean addEvent(Event event) {
        return super.add(event);
    }

    public boolean removeEventById(int i) {
        return super.remove(i);
    }

    public ArrayList<Event> getEventList() {
        return arrayList;
    }
}
