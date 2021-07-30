package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.list.ListSorter;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;

import java.util.ArrayList;

public class Group extends Item {
    private String groupName;
    private String alias;
    private int groupId;
    private int nbNewMessages;
    private UserList members;
    private MessageList messageList;
    private int ack;

    public Group() {
        members = new UserList();
        messageList = new MessageList();
        nbNewMessages = 0;
        ack = -1;
    }

    @Override
    public void update(Item item) {
        Group group = (Group) item;
        setGroupName(group.getGroupName());
        setAlias(group.getAlias());
        setNbNewMessages(group.getNbNewMessages());
        setMembers(group.getMembers());
    }

    public void refreshNbNewMessages() {
        Session session = Session.getInstance();
        MessageList messageList = getMessageList();
        ArrayList<PokoMessage> messages = messageList.getList();

        ListSorter<Integer, PokoMessage> sorter =
                (ListSorter<Integer, PokoMessage>) messageList.getListSorter();
        int startIndex = sorter.findAddPositionWithBsByKey(getAck()) + 1;
        int unackedNewMessages = 0;

        for (int i = startIndex; i < messages.size(); i++) {
            PokoMessage message = messages.get(i);
            if (!message.isMyMessage(session)) {
                unackedNewMessages++;
            }
        }
        setNbNewMessages(unackedNewMessages);
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

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }
}
