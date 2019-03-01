package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.ListSorter;
import com.murphy.pokotalk.data.SortingList;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/** Message list for message ListView in ChatActivity.
 * It has day change border feature.
 */
public class MessageListUI extends SortingList<Integer, PokoMessage> {
    private HashMap<Integer, PokoMessage> sentMessages;

    public MessageListUI() {
        super();
        sentMessages = new HashMap<>();
    }

    @Override
    public Integer getKey(PokoMessage message) {
        int key = message.getMessageId() * 8;
        switch (message.getMessageType()) {
            case PokoMessage.APP_DATE_MESSAGE: {
                key -= 1;
                break;
            }
        }
        return key;
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

    @Override
    protected int addHashMapAndArrayList(PokoMessage message) {
        int index = super.addHashMapAndArrayList(message);

        if (arrayList.size() > index + 1) {
            PokoMessage nextMessage = arrayList.get(index + 1);

            // Add date change message if date differ from next message
            if (nextMessage.getMessageType() != PokoMessage.APP_DATE_MESSAGE
                    && doesMessageDateChange(message, nextMessage)) {
                addHashMapAndArrayList(index + 1, makeDateChangeMessage(nextMessage));
            }
        }

        if (index - 1 >= 0) {
            PokoMessage prevMessage = arrayList.get(index - 1);

            // Add date change message if date differ from prev message
            if (prevMessage.getMessageType() != PokoMessage.APP_DATE_MESSAGE
                    && doesMessageDateChange(prevMessage, message)) {
                addHashMapAndArrayList(index, makeDateChangeMessage(message));
            }
        }

        return index;
    }

    @Override
    public PokoMessage removeItemByKey(Integer key) {
        PokoMessage curMessage = getItemByKey(key);
        if (curMessage == null) {
            return null;
        }

        int index = arrayList.indexOf(curMessage);
        if (index - 1 >= 0) {
            PokoMessage prevMessage = arrayList.get(index - 1);
            PokoMessage nextMessage = null;
            if (arrayList.size() > index + 1) {
                nextMessage = arrayList.get(index + 1);
            }

            /* Remove date change message if needed */
            if (prevMessage.getMessageType() == PokoMessage.APP_DATE_MESSAGE
                    && (nextMessage == null
                    || nextMessage.getMessageType() == PokoMessage.APP_DATE_MESSAGE)) {
                super.removeItemByKey(getKey(prevMessage));
            }
        }

        return super.removeItemByKey(key);
    }

    protected PokoMessage makeDateChangeMessage(PokoMessage messageNextDate) {
        SimpleDateFormat dateChangeFormat = new SimpleDateFormat(Constants.chatDateChangeFormat);
        dateChangeFormat.setTimeZone(Constants.timeZone);
        PokoMessage dateChangeMessage = new PokoMessage();
        dateChangeMessage.setMessageId(messageNextDate.getMessageId());
        dateChangeMessage.setMessageType(PokoMessage.APP_DATE_MESSAGE);
        dateChangeMessage.setSpecialContent(dateChangeFormat.format(messageNextDate.getDate().getTime()));
        dateChangeMessage.setSurviveOnListUpdate(true);

        return dateChangeMessage;
    }

    protected boolean doesMessageDateChange(PokoMessage prev, PokoMessage next) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(Constants.timeZone);
        int addDate = Integer.parseInt(format.format(prev.getDate().getTime()));
        int nextDate = Integer.parseInt(format.format(next.getDate().getTime()));
        return addDate < nextDate;
    }
}
