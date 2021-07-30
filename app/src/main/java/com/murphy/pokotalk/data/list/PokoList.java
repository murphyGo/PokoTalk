package com.murphy.pokotalk.data.list;

import com.murphy.pokotalk.data.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* PokoList that manages array of items with ArrayList and HashMap
   to efficiently insert and retrieve and remove items */
public abstract class PokoList<K, V extends Item> {
    protected HashMap<K, V> hashMap;
    protected ArrayList<V> arrayList;

    public PokoList() {
        hashMap = new HashMap<>();
        arrayList = new ArrayList<>();
    }

    public abstract K getKey(V v);

    public boolean add(V v) {
        V exist = getItemByKey(getKey(v));
        if (exist != null)
            return false;

        addHashMapAndArrayList(arrayList.size(), v);
        return true;
    }

    protected void addHashMapAndArrayList(int index, V v) {
        hashMap.put(getKey(v), v);
        arrayList.add(index, v);
    }

    public V removeItemByKey(K k) {
        V v = hashMap.remove(k);
        if (v == null)
            return null;

        arrayList.remove(v);
        return v;
    }

    public V getItemByKey(K key) {
        return hashMap.get(key);
    }

    public ArrayList<V> getList() {
        return arrayList;
    }

    public Iterator<V> iterator() {
        return getList().iterator();
    }
}
