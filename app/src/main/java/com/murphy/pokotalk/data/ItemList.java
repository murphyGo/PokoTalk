package com.murphy.pokotalk.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** ItemList adds functionality updating whole list so that
 *  each updated items are marked and unmarked items are removed
 *  at the end of update.
 */
public abstract class ItemList<K, V extends Item> extends List<K, V> {
    protected HashMap<K, V> updatedItems;
    private boolean updateListStarted;

    public ItemList() {
        super();
        updatedItems = new HashMap<>();
    }

    /** Mark the item updated.
     * Child classes must override this and call super.updateItem
     * @Return true if item was updated, false if item does not exist and is added
     */
    public boolean updateItem(V item) {
        if (updateListStarted) {
            updatedItems.put(getKey(item), item);
        }

        V found = getItemByKey(getKey(item));
        if (found == null) {
            add(item);
            return false;
        } else {
            found.update(item);
            return true;
        }
    }

    /** Starts updating whole items in list.
     */
    public void startUpdateList() {
        if (updateListStarted)
            return;

        updatedItems.clear();
        updateListStarted = true;
    }

    /** Ends updating whole items in list.
     *  Items not updated will be removed from the list
     */
    public void endUpdateList() {
        if (!updateListStarted)
            return;

        Iterator iter = iterator();
        ArrayList<V> notUpdatedItems = new ArrayList<>();
        while(iter.hasNext()) {
            V item = (V) iter.next();
            if (updatedItems.get(getKey(item)) == null)
                notUpdatedItems.add(item);
        }

        for (int i = 0; i < notUpdatedItems.size(); i++) {
            removeItemByKey(getKey(notUpdatedItems.get(i)));
        }

        updateListStarted = false;
    }

    public void copyFromPokoList(ItemList<K, V> list) {
        startUpdateList();
        for (V item : list.arrayList) {
            updateItem(item);
        }
        endUpdateList();
    }

    public void updateAll(ArrayList<V> items) {
        for (V item : items) {
            updateItem(item);
        }
    }

    public void removeAll(ArrayList<V> items) {
        for (V item : items) {
            removeItemByKey(getKey(item));
        }
    }
}
