package com.murphy.pokotalk.data.event;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.user.UserPokoList;

import java.util.Calendar;

public class PokoEvent extends Item {
    protected int eventId;
    protected String eventName;
    protected String description;
    protected UserPokoList participants;
    protected Calendar eventDate;
    protected int state;
    protected int ack;
    protected EventLocation location;
    protected Group group;

    // Event state
    public static final int EVENT_UPCOMING = 0;
    public static final int EVENT_STARTED = 1;

    // Event ack
    public static final int ACK_NOT_SEEN = 2;
    public static final int ACK_SEEN = 3;
    public static final int ACK_SEEN_STARTED = 4;

    public PokoEvent() {
        participants = new UserPokoList();
        group = null;
        location = null;
    }

    @Override
    public void update(Item item) {
        PokoEvent event = (PokoEvent) item;
        setEventName(event.getEventName());
        setDescription(event.getDescription());
        setParticipants(event.getParticipants());
        setEventDate(event.getEventDate());
        setState(event.getState());
        setGroup(event.getGroup());
        setAck(event.getAck());

        EventLocation location = getLocation();
        EventLocation location2 = event.getLocation();
        if (location != null && location2 != null) {
            location.update(location2);
        } else {
            setLocation(location2);
        }
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

    public UserPokoList getParticipants() {
        return participants;
    }

    public void setParticipants(UserPokoList participants) {
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

    public EventLocation getLocation() {
        return location;
    }

    public void setLocation(EventLocation location) {
        this.location = location;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }
}
