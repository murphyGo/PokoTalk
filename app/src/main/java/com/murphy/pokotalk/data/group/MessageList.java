package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.ItemList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MessageList extends ItemList<Integer, Message> {
    private HashMap<Integer, Message> sentMessages;
    private ArrayList<Message> unackedMessages;

    public MessageList() {
        super();
        sentMessages = new HashMap<>();
        unackedMessages = new ArrayList<>();
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
    public Message removeItemByKey(Integer key) {
        Message message = hashMap.get(key);
        if (message != null)
            unackedMessages.remove(message);
        return super.removeItemByKey(key);
    }

    @Override
    public boolean updateItem(Message item) {
        super.updateItem(item);

        Message message = getItemByKey(getKey(item));
        if (message == null) {
            addMessageSortedById(item);
            return false;
        } else {
            message.setNbNotReadUser(item.getNbNotReadUser());
            message.setDate(item.getDate());
            message.setContent(item.getContent());
            message.setMessageType(item.getMessageType());
            return true;
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
        return addMessageSortedById(sentMessage);
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
                toIndex = findPositionWithBinarySearch(toId);
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

    private int findPositionWithBinarySearch(int toId) {
        int size = arrayList.size();
        int start = 0, end = size - 1;
        int curIndex, properIndex = 0;
        while (start <= end) {
            curIndex = (start + end) / 2;
            Message curMessage = arrayList.get(curIndex);
            int curId = curMessage.getMessageId();
            if (curId == toId) {
                return curIndex;
            } else if (curId < toId) {
                start = curIndex + 1;
                properIndex = start - 1;
            } else {
                end = curIndex - 1;
                properIndex = end;
            }
        }

        return properIndex < 0 ? 0 : properIndex;
    }

    private boolean inRange(int fromId, int toId, int targetId) {
        return fromId <= targetId && toId >= targetId;
    }

    /* Message add sorted and sort method */
    public boolean addMessageSortedById(Message message) {
        Message exist = getItemByKey(getKey(message));
        if (exist != null)
            return false;

        // TODO: Improve with binary search when it takes long to find location
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

    public void sortMessagesByMessageId() {
        Collections.sort(arrayList, new MessageComparator());
    }

    @Override
    public Integer getKey(Message message) {
        return message.getMessageId();
    }

    /* Comparator class for sorting message by messageId */
    class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1, Message o2) {
            int id1 = o1.getMessageId(), id2 = o2.getMessageId();
            if (id1 < id2)
                return -1;
            else if (id1 > id2)
                return 1;
            else
                return 0;
        }
    }
}
