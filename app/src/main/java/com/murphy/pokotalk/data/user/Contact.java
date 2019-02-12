package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.Item;

import java.util.Calendar;

public class Contact extends User {
    private Calendar lastSeen;

    public Contact() {
        super();
    }

    public Contact(int id, String email, String nickname, String picture, Calendar lastSeen) {
        super(id, email, nickname, picture);
        this.lastSeen = lastSeen;
    }

    @Override
    public void update(Item item) {
        super.update(item);

        Contact contact = (Contact) item;
        setLastSeen(contact.getLastSeen());
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }
}
