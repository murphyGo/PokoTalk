package com.murphy.pokotalk.data.group;

import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.ListSorter;
import com.murphy.pokotalk.data.SortingList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/** Message list for message ListView in ChatActivity.
 * It has date change messaging feature.
 */
public class MessageListUI extends SortingList<Integer, PokoMessage> {
    private HashMap<Integer, PokoMessage> sentMessages;

    public MessageListUI() {
        super();
        sentMessages = new HashMap<>();
    }

    // Key is expanded by factor 8 to have room for 7 app messages
    // such as date change message.
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

            if (nextMessage.getMessageType() == PokoMessage.APP_DATE_MESSAGE) {
                if (compareMessageDate(message, nextMessage) == 0) {
                    // If next message is date change message with date of added message,
                    // remove it and create on top of added message.
                    super.removeItemByKey(getKey(nextMessage));
                }
            } else if (compareMessageDate(message, nextMessage) < 0) {
                // Add date change message if date differ from next message
                addHashMapAndArrayList(index + 1, makeDateChangeMessage(nextMessage));
            }
        }

        // If this is a message on top, add date change message on top
        if (index == 0) {
            addHashMapAndArrayList(0, makeDateChangeMessage(message));
        } else if (index - 1 >= 0) {
            PokoMessage prevMessage = arrayList.get(index - 1);

            // Add date change message if date differ from prev message
            if (prevMessage.getMessageType() != PokoMessage.APP_DATE_MESSAGE
                    && compareMessageDate(prevMessage, message) < 0) {
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
        SimpleDateFormat dateChangeFormat = new SimpleDateFormat(Constants.chatDateChangeFormat, Constants.locale);
        dateChangeFormat.setTimeZone(Constants.timeZone);
        PokoMessage dateChangeMessage = new PokoMessage();
        dateChangeMessage.setMessageId(messageNextDate.getMessageId());
        dateChangeMessage.setMessageType(PokoMessage.APP_DATE_MESSAGE);
        dateChangeMessage.setSpecialContent(dateChangeFormat.format(messageNextDate.getDate().getTime()));
        // date will be midnight of that day
        Calendar date = (Calendar) messageNextDate.getDate().clone();
        date.setTimeZone(Constants.timeZone);
        date.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY) - date.get(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        dateChangeMessage.setDate(date);
        dateChangeMessage.setSurviveOnListUpdate(true);


        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        format.setTimeZone(Constants.timeZone);
        Log.v("POKO", "content " + messageNextDate.getContent() + ", " + format.format(date.getTime()));

        return dateChangeMessage;
    }

    // compares 'date' of message by year, month, day
    // returns 0 if same, -1 if prev is smaller than next, 1 if prev is greater than next
    protected int compareMessageDate(PokoMessage prev, PokoMessage next) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        format.setTimeZone(Constants.timeZone);
        int prevDate = Integer.parseInt(format.format(prev.getDate().getTime()));
        int nextDate = Integer.parseInt(format.format(next.getDate().getTime()));

        if (prevDate == nextDate) {
            return 0;
        } else if (prevDate < nextDate) {
            return -1;
        } else {
            return 1;
        }
    }
}
