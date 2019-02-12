package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

public class Group extends Item {
    private String groupName;
    private String alias;
    private int groupId;
    private int nbNewMessages;
    private UserList members;
    private MessageList messageList;

    public Group() {
        members = new UserList();
        messageList = new MessageList();
        nbNewMessages = 0;
    }

    @Override
    public void update(Item item) {
        Group group = (Group) item;
        setGroupName(group.getGroupName());
        setAlias(group.getAlias());
        setNbNewMessages(group.getNbNewMessages());
        setMembers(group.getMembers());
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

    public User removeMember(User member) {
        return this.members.removeItemByKey(this.members.getKey(member));
    }

    public MessageList getMessageList() {
        return messageList;
    }

    public void setMessageList(MessageList messages) {
        this.messageList = messages;
    }
}
