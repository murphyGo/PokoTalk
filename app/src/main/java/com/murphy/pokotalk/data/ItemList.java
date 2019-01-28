package com.murphy.pokotalk.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** ItemList adds functionality updating whole list so that
 *  each updated items are marked and unmarked items are removed
 *  at the end of update.
 */
public abstract class ItemList<K, V> extends List<K, V> {
    protected HashMap<K, V> updatedItems;
    private boolean updateListStarted;

    public ItemList() {
        super();
        updatedItems = new HashMap<>();
    }

    /** Mark the item updated.
     * Child classes must override this and call super.updateItem
     */
    public void updateItem(V item) {
        updatedItems.put(getKey(item), item);
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
}
