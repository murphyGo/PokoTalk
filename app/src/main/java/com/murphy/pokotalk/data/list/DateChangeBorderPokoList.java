package com.murphy.pokotalk.data.list;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.Item;
import com.murphy.pokotalk.data.extra.DateChangeItem;
import com.murphy.pokotalk.data.extra.ExtraItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/** DateChangeBorderPokoList adds functionality that inserts date chane border message
 * between items */
public abstract class DateChangeBorderPokoList<K, L, V extends Item> extends SortingPokoList<K, V> {
    protected boolean countDateChangeMessage;
    protected int dateChangeMessageNumDelta;
    protected ArrayList<Item> dateChangeList;
    protected ArrayList<ExtraItem<L>> extraItems;
    protected ListSorter<L, ExtraItem<L>> extraListSorter;

    protected abstract L getTimeKey(V item);
    protected abstract Calendar getDate(V item);
    protected abstract int compareTwoKey(L key1, L key2);
    public abstract boolean isInstanceof(Item item);

    public DateChangeBorderPokoList() {
        super();
        countDateChangeMessage = false;
        dateChangeList = new ArrayList<>();
        extraItems = new ArrayList<>();

        extraListSorter = new ListSorter<L, ExtraItem<L>>(extraItems) {
            @Override
            public L getItemKey(ExtraItem<L> item) {
                return item.getKey();
            }

            @Override
            public int compareKey(L key1, L key2) {
                return compareTwoKey(key1, key2);
            }
        };
    }

    public ArrayList<Item> getListForAdapter() {
        return dateChangeList;
    }

    public void startCountDateChangeMessage() {
        countDateChangeMessage = true;
        dateChangeMessageNumDelta = 0;
    }

    public int endCountDateChangeMessage() {
        countDateChangeMessage = false;
        return dateChangeMessageNumDelta;
    }

    @Override
    protected int addHashMapAndArrayList(V item) {
        int index = super.addHashMapAndArrayList(item);
        int extraNum = extraListSorter.findAddPositionWithBsByKey(getTimeKey(item));

        // Calculate index in date change list
        index += extraNum;
        dateChangeList.add(index, item);

        if (dateChangeList.size() > index + 1) {
            Item nextItem = dateChangeList.get(index + 1);

            if (isDateChangeItem(nextItem)) {
                if (compareItemDate(item, nextItem) == 0) {
                    // If next message is date change message with date of added message,
                    // remove it and create on top of added message.
                    DateChangeItem<L> dateChangeItem =
                            (DateChangeItem) dateChangeList.remove(index + 1);
                    extraListSorter.removeItem(dateChangeItem);

                    if (countDateChangeMessage) {
                        dateChangeMessageNumDelta--;
                    }
                }
            } else if (compareItemDate(item, nextItem) != 0) {
                // Add date change message if date differ from next message
                DateChangeItem<L> dateChangeItem = makeDateChangeItem(nextItem);
                dateChangeList.add(index + 1, dateChangeItem);
                extraListSorter.addItemSorted(dateChangeItem);

                if (countDateChangeMessage) {
                    dateChangeMessageNumDelta++;
                }
            }
        }

        // If this is a message on top, add date change message on top
        if (index == 0) {
            DateChangeItem<L> dateChangeItem = makeDateChangeItem(item);
            dateChangeList.add(0, dateChangeItem);
            extraListSorter.addItemSorted(dateChangeItem);

            if (countDateChangeMessage) {
                dateChangeMessageNumDelta++;
            }
        } else if (index - 1 >= 0) {
            Item prevItem = dateChangeList.get(index - 1);

            // Add date change message if date differ from prev message
            if (!isDateChangeItem(prevItem) && compareItemDate(prevItem, item) != 0) {
                DateChangeItem<L> dateChangeItem = makeDateChangeItem(item);
                dateChangeList.add(index, dateChangeItem);
                extraListSorter.addItemSorted(dateChangeItem);

                if (countDateChangeMessage) {
                    dateChangeMessageNumDelta++;
                }
            }
        }

        return index;
    }

    @Override
    public V removeItemByKey(K key) {
        V curItem = getItemByKey(key);
        if (curItem == null) {
            return null;
        }

        int index = dateChangeList.indexOf(curItem);
        int extraNum = extraListSorter.findAddPositionWithBsByKey(getTimeKey(curItem));

        // Calculate index in date change
        index += extraNum;

        if (index - 1 >= 0) {
            Item prevItem = dateChangeList.get(index - 1);
            Item nextItem = null;
            if (dateChangeList.size() > index + 1) {
                nextItem = dateChangeList.get(index + 1);
            }

            /* Remove date change message if needed */
            if (isDateChangeItem(prevItem)
                    && (nextItem == null
                    || isDateChangeItem(nextItem))) {
                DateChangeItem<L> dateChangeItem =
                        (DateChangeItem) dateChangeList.remove(index - 1);
                extraListSorter.removeItem(dateChangeItem);
            }
        }

        return super.removeItemByKey(key);
    }

    protected boolean isDateChangeItem(Item item) {
        return item instanceof DateChangeItem;
    }

    protected DateChangeItem<L> makeDateChangeItem(Item item) {
        DateChangeItem<L> dateChangeItem = new DateChangeItem<>();
        SimpleDateFormat dateChangeFormat = new SimpleDateFormat(Constants.chatDateChangeFormat, Constants.locale);
        dateChangeFormat.setTimeZone(Constants.timeZone);
        Calendar date;

        if (item instanceof ExtraItem) {
            ExtraItem<L> extraItem = (ExtraItem<L>) item;
            date = extraItem.getDate();
            dateChangeItem.setKey(extraItem.getKey());
            dateChangeItem.setDateChangeMessage(dateChangeFormat.format(date.getTime()));
        } else {
            V vItem = (V) item;
            date = getDate(vItem);
            dateChangeItem.setKey(getTimeKey(vItem));
            dateChangeItem.setDateChangeMessage(dateChangeFormat.format(date.getTime()));
        }

        // date will be midnight of that day
        Calendar dateClone = (Calendar) date.clone();
        dateClone.setTimeZone(Constants.timeZone);
        dateClone.set(Calendar.HOUR_OF_DAY,
                dateClone.get(Calendar.HOUR_OF_DAY) - dateClone.get(Calendar.HOUR_OF_DAY));
        dateClone.set(Calendar.MINUTE, 0);
        dateClone.set(Calendar.SECOND, 0);
        dateClone.set(Calendar.MILLISECOND, 0);
        dateChangeItem.setDate(dateClone);

        return dateChangeItem;
    }

    protected int compareItemDate(Item prev, Item next) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        format.setTimeZone(Constants.timeZone);

        Calendar prevCalendar, nextCalendar;

        // Items must be type V or ExtraItem
        if (isInstanceof(prev)) {
            prevCalendar = getDate((V) prev);
        } else {
            prevCalendar = ((ExtraItem) prev).getDate();
        }

        if (isInstanceof(next)) {
            nextCalendar = getDate((V) next);
        } else {
            nextCalendar = ((ExtraItem) next).getDate();
        }

        int prevDate = Integer.parseInt(format.format(prevCalendar.getTime()));
        int nextDate = Integer.parseInt(format.format(nextCalendar.getTime()));

        if (prevDate == nextDate) {
            return 0;
        } else if (prevDate < nextDate) {
            return -1;
        } else {
            return 1;
        }
    }
}
