package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.list.ListSorter;
import com.murphy.pokotalk.data.list.SortingPokoList;
import com.murphy.pokotalk.data.user.User;

import java.util.Calendar;
import java.util.HashMap;

public class MessagePokoList extends SortingPokoList<Integer, PokoMessage> {
    private HashMap<Integer, PokoMessage> sentMessages;

    public MessagePokoList() {
        super();
        sentMessages = new HashMap<>();
    }

    @Override
    public Integer getKey(PokoMessage message) {
        return message.getMessageId();
    }

    @Override
    public ListSorter getListSorter() {
        return new ListSorter<Integer, PokoMessage>(getList()) {
            @Override
            public Integer getItemKey(PokoMessage item) {
                return getKey(item);
            }

            @Override
            public int compareKey(Integer key1, Integer key2) {
                return key1.compareTo(key2);
            }
        };
    }

    public PokoMessage getLastMessage() {
        if (arrayList.size() == 0)
            return null;

        return arrayList.get(arrayList.size() - 1);
    }

    /** Sent messages are messages sent by user but not acknowledged by server yet.
     *  We maintain these messages in some other list and move to message list
     *  when server has acked the message.
     */
    public void addSentMessage(PokoMessage message) {
        sentMessages.put(message.getSendId(), message);
    }

    public PokoMessage getSentMessage(int sendId) {
        return sentMessages.get(sendId);
    }

    public boolean moveSentMessageToMessageList(int sendId, int messageId, int nbread, Calendar date) {
        PokoMessage sentMessage = sentMessages.remove(sendId);
        if (sentMessage == null)
            return false;

        sentMessage.setMessageId(messageId);
        sentMessage.setDate(date);
        sentMessage.setNbNotReadUser(nbread);
        return add(sentMessage);
    }

    /* Acknowledges message method from fromId to toId inclusive. */
    public void ackMessages(int fromId, int toId, boolean decrement,
                            User ackUser) {
        if (toId < fromId)
            return;

        if (arrayList.size() == 0)
            return;

        PokoMessage toMessage = getItemByKey(toId);
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
                    toIndex = curIndex;
                    found = true;
                    break;
                }
            }

            if (!found) {
                toIndex = listSorter.findAddPositionWithBsByKey(toId) - 1;
                toIndex = toIndex < 0 ? 0 : toIndex;
            }
        } else {
            toIndex = arrayList.indexOf(toMessage);
        }

        /* Ack starts from toId back to fromId */
        ackFromBackToFront(fromId, toIndex, decrement, ackUser);
    }

    private void ackFromBackToFront(int fromId, int toIndex, boolean decrement, User ackUser) {
        /* When message with start id exists */
        int curIndex = toIndex;
        do {
            PokoMessage curMessage = arrayList.get(curIndex);
            if (curMessage == null || curMessage.getMessageId() < fromId)
                break;
            if (decrement && !ackUser.equals(curMessage.getWriter())) {
                curMessage.decrementNbNotReadUser();
            }
        } while (--curIndex >= 0);
    }

    private boolean inRange(int fromId, int toId, int targetId) {
        return fromId <= targetId && toId >= targetId;
    }
}
