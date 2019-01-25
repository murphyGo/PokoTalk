package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.group.Group;

import java.util.Calendar;

public class Contact extends User {
    private Integer contactId;
    private Integer groupId;
    private Calendar lastSeen;
    private Group chatGroup;

    public Contact() {
        super();
    }

    public Contact(int id, String email, String nickname, String picture, Calendar lastSeen) {
        super(id, email, nickname, picture);
        this.lastSeen = lastSeen;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Group getChatGroup() {
        return chatGroup;
    }

    public void setChatGroup(Group chatGroup) {
        this.chatGroup = chatGroup;
    }
}
