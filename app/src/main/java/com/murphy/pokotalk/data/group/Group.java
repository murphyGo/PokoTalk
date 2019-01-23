package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

import java.util.ArrayList;
import java.util.Calendar;

public class Group {
    private String groupName;
    private String alias;
    private int groupId;
    private int nbNewMessages;
    private UserList members;
    private ArrayList<Message> messages;
    private Calendar lastMessageDate;

    public Group() {
        members = new UserList();
        messages = new ArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Calendar getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Calendar lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
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

    public UserList getMembers() {
        return members;
    }

    public void setMembers(UserList members) {
        this.members = members;
    }

    public void addMember(User member) {
        this.members.add(member);
    }

    public boolean removeMember(User member) {
        return this.members.removeItemByKey(this.members.getKey(member));
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
