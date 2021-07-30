package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.list.DateChangeBorderPokoList;
import com.murphy.pokotalk.data.list.ListSorter;

import java.util.Calendar;
import java.util.HashMap;

/** Message list for message ListView in ChatActivity.
 * It has date change messaging feature.
 */
public class MessageListUI extends DateChangeBorderPokoList<Integer, Integer, PokoMessage> {
    protected HashMap<Integer, PokoMessage> sentMessages;

    public MessageListUI() {
        super();
        sentMessages = new HashMap<>();
    }

    @Override
    protected Integer getTimeKey(PokoMessage item) {
        return item.getMessageId();
    }

    @Override
    protected int compareTwoKey(Integer key1, Integer key2) {
        return key1.compareTo(key2);
    }

    @Override
    public boolean isInstanceof(Item item) {
        return item instanceof PokoMessage;
    }

    @Override
    protected Calendar getDate(PokoMessage item) {
        return item.getDate();
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
}
