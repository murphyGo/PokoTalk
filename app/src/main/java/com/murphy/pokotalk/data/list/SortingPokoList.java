package com.murphy.pokotalk.data.list;

import com.murphy.pokotalk.data.Item;

/** SortingPokoList adds functionality internal ArrayList is sorted.
 */
public abstract class SortingPokoList<K, V extends Item> extends ItemPokoList<K, V> {
    protected ListSorter listSorter;

    public SortingPokoList() {
        super();
        listSorter = getListSorter();
    }

    public abstract ListSorter getListSorter();

    @Override
    public boolean add(V v) {
        V exist = getItemByKey(getKey(v));
        if (exist != null)
            return false;

        addHashMapAndArrayList(v);
        return true;
    }

    protected int addHashMapAndArrayList(V v) {
        hashMap.put(getKey(v), v);
        return listSorter.addItemSorted(v);
    }

    public void moveItemSortedByKey(V item) {
        if (item != null) {
            if (arrayList.remove(item)) {
                listSorter.addItemSorted(item);
            }
        }
    }

    public void sortItemsByKey() {
        listSorter.sortList();
    }
}
