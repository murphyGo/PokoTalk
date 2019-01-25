package com.murphy.pokotalk.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* List that manages array of items with ArrayList and HashMap
   to efficiently insert and retrieve and remove items */
public abstract class List<K, V> {
    protected HashMap<K, V> hashMap;
    protected ArrayList<V> arrayList;

    public List() {
        hashMap = new HashMap<>();
        arrayList = new ArrayList<>();
    }

    public abstract K getKey(V v);

    public boolean add(V v) {
        V exist = hashMap.get(getKey(v));
        if (exist != null)
            return false;

        hashMap.put(getKey(v), v);
        arrayList.add(v);
        return true;
    }

    public V removeItemByKey(K k) {
        V v = hashMap.get(k);
        if (v == null)
            return null;

        arrayList.remove(hashMap.remove(k));
        return v;
    }

    public V getItemByKey(K key) {
        return hashMap.get(key);
    }

    public ArrayList<V> getList() {
        return arrayList;
    }

    public Iterator<V> iterator() {
        return arrayList.iterator();
    }
}
