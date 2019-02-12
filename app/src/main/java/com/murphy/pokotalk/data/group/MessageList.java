package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.ListSorter;
import com.murphy.pokotalk.data.SortingList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MessageList extends SortingList<Integer, Message> {
    private HashMap<Integer, Message> sentMessages;
    private ArrayList<Message> unackedMessages;

    public MessageList() {
        super();
        sentMessages = new HashMap<>();
        unackedMessages = new ArrayList<>();
    }

    @Override
    public Integer getKey(Message message) {
        return message.getMessageId();
    }

    @Override
    public ListSorter getListSorter() {
        return new ListSorter<Integer, Message>(getList()) {
            @Override
            public Integer getKey(Message item) {
                return item.getMessageId();
            }

            @Override
            public int compareKey(Integer key1, Integer key2) {
                return key1.compareTo(key2);
            }
        };
    }

    public ArrayList<Message> getUnackedMessages() {
        return unackedMessages;
    }

    public Message getLastMessage() {
        if (arrayList.size() == 0)
            return null;

        return arrayList.get(arrayList.size() - 1);
    }

    @Override
    protected void addHashMapAndArrayList(int index, Message message) {
        if (!message.isAcked())
            unackedMessages.add(message);
        super.addHashMapAndArrayList(index, message);
    }

    @Override
    protected void addHashMapAndArrayList(Message message) {
        if (!message.isAcked())
            unackedMessages.add(message);
        super.addHashMapAndArrayList(message);
    }

    @Override
    public Message removeItemByKey(Integer key) {
        Message message = hashMap.get(key);
        if (message != null)
            unackedMessages.remove(message);
        return super.removeItemByKey(key);
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
        return add(sentMessage);
    }

    /* Acknowledges message method from fromId to toId inclusive. */
    public void ackMessages(int fromId, int toId, boolean decrement, boolean markAcked) {
        if (toId < fromId)
            return;

        if (arrayList.size() == 0)
            return;

        Message toMessage = getItemByKey(toId);
        int toIndex = 0;
        /* When message with start id does not exists,
         * Check if last three messages are in range.
         * If so, start from there.
         * If not, find proper position with binary search. */
        if (toMessage == null) {
            boolean found = false;
            for (int step = 1; step <= 3; step++) {
                int curIndex = arrayList.size() - step;
                if (curIndex < 0)
                    break;
                int messageId = arrayList.get(curIndex).getMessageId();
                if (inRange(fromId, toId, messageId)) {
                    toIndex = messageId;
                    found = true;
                    break;
                }
            }

            if (!found) {
                toIndex = listSorter.findAddPositionWithBS(toId) - 1;
                toIndex = toIndex < 0 ? 0 : toIndex;
            }
        } else {
            toIndex = arrayList.indexOf(toMessage);
        }

        /* Ack starts from toId back to fromId */
        ackFromBackToFront(fromId, toIndex, decrement, markAcked);
    }

    private void ackFromBackToFront(int fromId, int toIndex, boolean decrement, boolean markAcked) {
        /* When message with start id exists */
        int curIndex = toIndex;
        do {
            Message curMessage = arrayList.get(curIndex);
            if (curMessage == null || curMessage.getMessageId() < fromId)
                break;
            if (decrement)
                curMessage.decrementNbNotReadUser();
            if (markAcked && !curMessage.isAcked()) {
                curMessage.setAcked(true);
                unackedMessages.remove(curMessage);
            }
        } while (--curIndex >= 0);
    }

    private boolean inRange(int fromId, int toId, int targetId) {
        return fromId <= targetId && toId >= targetId;
    }
}
