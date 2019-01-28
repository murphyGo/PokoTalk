package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.ItemList;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MessageList extends ItemList<Integer, Message> {
    private HashMap<Integer, Message> sentMessages;

    public MessageList() {
        super();
        sentMessages = new HashMap<>();
    }

    @Override
    public void updateItem(Message item) {
        super.updateItem(item);

        Message message = getItemByKey(getKey(item));
        if (message == null) {
            add(item);
        } else {
            message.setNbNotReadUser(item.getNbNotReadUser());
            message.setDate(item.getDate());
            message.setContent(item.getContent());
            message.setMessageType(item.getMessageType());
        }
    }

    /** Sent messages are messages sent by user but not acknowledged by server yet.
     *  We maintain these messages in some other list and move to message list
     *  when server has acked the message.
     */
    public void addSentMessage(Message message) {
        sentMessages.put(message.getSendId(), message);
    }

    public Message getSentMessage(int sendId) {
        return sentMessages.get(sendId);
    }

    public boolean moveSentMessageToMessageList(int sendId, int messageId, int nbread, Calendar date) {
        Message sentMessage = sentMessages.remove(sendId);
        if (sentMessage == null)
            return false;

        sentMessage.setMessageId(messageId);
        sentMessage.setDate(date);
        sentMessage.setNbNotReadUser(nbread);
        return addMessageSortedByTime(sentMessage);
    }

    /* Acknowledges message method from fromId to toId inclusive. */
    public void ackMessages(int fromId, int toId) {
        if (toId < fromId)
            return;

        Message fromMessage = getItemByKey(fromId);
        if (fromMessage == null) {
            for (int i = arrayList.size(); i >= 0; i--) {
                Message curMessage = arrayList.get(i);
                if (curMessage.getMessageId() < fromId)
                    break;
                if (curMessage.getMessageId() <= toId)
                    curMessage.decrementNbNotReadUser();
            }
        } else {
            int curIndex = arrayList.indexOf(fromMessage);
            do {
                Message curMessage = arrayList.get(curIndex);
                if (curMessage.getMessageId() > toId)
                    break;
                curMessage.decrementNbNotReadUser();
            } while (++curIndex < arrayList.size());
        }
    }

    /* Message add sorted and sort method */
    public boolean addMessageSortedByTime(Message message) {
        Message exist = getItemByKey(getKey(message));
        if (exist != null)
            return false;

        for (int i = arrayList.size() - 1; i >= 0; i--) {
            Message curMessage = arrayList.get(i);
            if (curMessage.getDate().compareTo(message.getDate()) <= 0) {
                addHashMapAndArrayList(i + 1, message);
                return true;
            }
        }

        addHashMapAndArrayList(0, message);
        return true;
    }

    public void sortMessagesByTime() {
        Collections.sort(arrayList, new MessageComparator());
    }

    @Override
    public Integer getKey(Message message) {
        return message.getMessageId();
    }

    /* Comparator class for sorting message by time */
    class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1, Message o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }
}
