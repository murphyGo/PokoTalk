package com.murphy.pokotalk.data;

import java.util.ArrayList;

public class Group {
    private String groupName;
    private int groupId;
    private int nbNewMessages;
    private ContactList members;
    private ArrayList<Message> messages;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getNbNewMessages() {
        return nbNewMessages;
    }

    public void setNbNewMessages(int nbNewMessages) {
        this.nbNewMessages = nbNewMessages;
    }

    public ContactList getMembers() {
        return members;
    }

    public void setMembers(ContactList members) {
        this.members = members;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
