package com.murphy.pokotalk.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Sort, Insert, remove to/from ArrayList in a way that it maintains sorted */
public abstract class ListSorter<K, V> {
    protected List<V> list;
    public abstract K getKey(V item);
    public abstract int compareKey(K key1, K key2);
    public static final int INSERT_TRYIAL = 3;

    public ListSorter(List<V> list) {
        this.list = list;
    }

    /** Finds appropriate add position that makes list sorted with binary search */
    public int findAddPositionWithBS(V item) {
        K key = getKey(item);
        int size = list.size();
        int start = 0, end = size - 1;
        int curIndex, properIndex = 0;
        while (start <= end) {
            curIndex = (start + end) / 2;
            V curItem = list.get(curIndex);
            K curKey = getKey(curItem);
            int cmp = compareKey(curKey, key);
            if (cmp == 0) {
                return curIndex;
            } else if (cmp < 0) {
                start = curIndex + 1;
                properIndex = start;
            } else {
                end = curIndex - 1;
                properIndex = end + 1;
            }
        }

        return properIndex;
    }

    /** Finds appropriate item position in array with binary search */
    public int findItemIndexWithBS(V item) {
        K key = getKey(item);
        int size = list.size();
        int start = 0, end = size - 1;
        int curIndex, properIndex = 0;
        while (start <= end) {
            curIndex = (start + end) / 2;
            V curItem = list.get(curIndex);
            K curKey = getKey(curItem);
            int cmp = compareKey(curKey, key);
            if (cmp == 0) {
                return curIndex;
            } else if (cmp < 0) {
                start = curIndex + 1;
            } else {
                end = curIndex - 1;
            }
        }

        return -1;
    }

    /* PokoMessage add sorted and sort method */
    public boolean addItemSorted(V item) {
        if (list.size() == 0) {
            list.add(item);
            return true;
        }

        K itemKey = getKey(item);
        /* It tries last INSERT_TRIAL items to find location.
        /* If it fails, it coverts to binary search. */
        for (int i = 1; i <= INSERT_TRYIAL; i++) {
            int index = list.size() - i;
            if (index < 0) {
                list.add(0, item);
                return true;
            }
            V curItem = list.get(index);
            if (compareKey(getKey(curItem), itemKey) < 0) {
                list.add(index + 1, item);
                return true;
            }
        }

        int index = findAddPositionWithBS(item);
        list.add(index, item);
        return true;
    }

    /**NOTE: Remove item, if items with same key exists, it is unpredictable */
    public boolean removeItem(V item) {
        int index = findItemIndexWithBS(item);
        if (index < 0)
            return false;

        return list.remove(index) != null;
    }

    public void sortList() {
        Collections.sort(list, new ItemComparator());
    }

    /* Comparator class for sorting message by messageId */
    class ItemComparator implements Comparator<V> {
        @Override
        public int compare(V item1, V item2) {
            return compareKey(getKey(item1), getKey(item2));
        }
    }

}
