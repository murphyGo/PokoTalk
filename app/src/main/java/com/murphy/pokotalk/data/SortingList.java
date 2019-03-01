package com.murphy.pokotalk.data;

/** SortingList adds functionality internal ArrayList is sorted.
 */
public abstract class SortingList<K, V extends Item> extends ItemList<K, V>{
    protected ListSorter listSorter;

    public SortingList() {
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

    public void moveItemToFront(V item) {
        if (item != null && arrayList.remove(item)) {
            arrayList.add(0, item);
        }
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
