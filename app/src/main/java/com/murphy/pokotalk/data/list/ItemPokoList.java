package com.murphy.pokotalk.data.list;

import com.murphy.pokotalk.data.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** ItemPokoList adds functionality updating whole list so that
 *  each updated items are marked and unmarked items are removed
 *  at the end of update.
 */
public abstract class ItemPokoList<K, V extends Item> extends PokoList<K, V> {
    protected HashMap<K, V> updatedItems;
    private boolean updateListStarted;

    public ItemPokoList() {
        super();
        updatedItems = new HashMap<>();
    }

    /** Mark the item updated.
     * @Return updated existing item if item was updated,
     * or item given as argument if item does not exist and is added
     */
    public V updateItem(V item) {
        if (updateListStarted) {
            updatedItems.put(getKey(item), item);
        }

        V found = getItemByKey(getKey(item));
        if (found == null) {
            add(item);
            return item;
        } else {
            found.update(item);
            return found;
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
            V item = notUpdatedItems.get(i);
            if (!item.isSurviveOnListUpdate()) {
                removeItemByKey(getKey(notUpdatedItems.get(i)));
            }
        }

        updateListStarted = false;
    }

    public void copyFromPokoList(ItemPokoList list) {
        startUpdateList();
        for (Object item : list.arrayList) {
            updateItem((V) item);
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
