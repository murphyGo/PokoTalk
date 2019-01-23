package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.user.ContactList;

import java.util.Calendar;

public class Event {
    private int eventId;
    private String eventName;
    private String description;
    private ContactList participants;
    private Calendar eventDate;
    private int state;

    public static final int EVENT_UPCOMING = 0;
    public static final int EVENT_STARTED = 1;

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
}
