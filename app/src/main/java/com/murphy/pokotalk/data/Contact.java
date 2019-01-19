package com.murphy.pokotalk.data;

import java.util.Calendar;

public class Contact {
    private int userId;
    private int contactId;
    private int groupId;
    private String email;
    private String nickname;
    private String picture;
    private Calendar lastSeen;
    private Group chatGroup;

    public Contact() {

    }

    public Contact(int id, String email, String nickname, String picture, Calendar lastSeen) {
        this.userId = id;
        this.email = email;
        this.nickname = nickname;
        this.picture = picture;
        this.lastSeen = lastSeen;

    }

    @Override
    public boolean equals(Object i) {
        if (i instanceof Integer) {
            return userId == (Integer) i;
        } else if (i instanceof Contact) {
            return userId == ((Contact) i).getUserId();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }
}
