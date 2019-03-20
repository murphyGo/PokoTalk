package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.user.ContactList;

import java.util.Calendar;

public class Event extends Item {
    protected int eventId;
    protected String eventName;
    protected String description;
    protected ContactList participants;
    protected Calendar eventDate;
    protected int state;
    protected Group group;

    public static final int EVENT_UPCOMING = 0;
    public static final int EVENT_STARTED = 1;

    @Override
    public void update(Item item) {
        Event event = (Event) item;
        setEventName(event.getEventName());
        setDescription(event.getDescription());
        setParticipants(event.getParticipants());
        setEventDate(event.getEventDate());
        setState(event.getState());
        setGroup(event.getGroup());
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ContactList getParticipants() {
        return participants;
    }

    public void setParticipants(ContactList participants) {
        this.participants = participants;
    }

    public Calendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
