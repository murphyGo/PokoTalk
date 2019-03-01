package com.murphy.pokotalk.data.group;

import android.util.Log;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.ListSorter;
import com.murphy.pokotalk.data.SortingList;
import com.murphy.pokotalk.data.user.ContactList;

import java.util.Calendar;
import java.util.HashMap;

/** Group list for user interface(ListView adapter).
 * It sorts group in latest message order.
 * Also it does not show contact chat with no message.
 */
public class GroupListUI extends SortingList<Integer, Group> {
    protected ContactList contactList;
    protected HashMap<Integer, Group> contactChatGroupWithNoMessage;

    public GroupListUI() {
        super();
        contactList = DataCollection.getInstance().getContactList();
        contactChatGroupWithNoMessage = new HashMap<>();
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }

    @Override
    public ListSorter getListSorter() {
        return new ListSorter<Calendar, Group>(getList()) {
            @Override
            public Calendar getItemKey(Group item) {
                MessageList messageList = item.getMessageList();
                PokoMessage lastMessage = messageList.getLastMessage();
                if (lastMessage == null)
                    return null;
                return lastMessage.getDate();
            }

            @Override
            public int compareKey(Calendar key1, Calendar key2) {
                if (key1 == null && key2 == null)
                    return 0;
                else if (key1 == null)
                    return 1;
                else if (key2 == null)
                    return -1;

                return key2.compareTo(key1);
            }
        };
    }

    @Override
    protected int addHashMapAndArrayList(Group group) {
        ContactList.ContactGroupRelation relation =
                contactList.getContactGroupRelationByGroupId(group.getGroupId());
        Log.v("ADD GROUP TO LIST", group.getGroupId() + " relation " +
                (relation == null ? "null" : relation.toString()));
        if (relation != null && group.getMessageList().getLastMessage() == null) {
            contactChatGroupWithNoMessage.put(getKey(group), group);
            return -1;
        } else {
            Log.v("size of arraylist", arrayList.size() + "");
            return super.addHashMapAndArrayList(group);
        }
    }

    public boolean addContactChatGroupIfHasMessage(Group group) {
        if (group.getMessageList().getLastMessage() != null) {
            Group removedGroup = contactChatGroupWithNoMessage.remove(getKey(group));
            Log.v("Removed", removedGroup == null ? "null" : removedGroup.toString());
            if (removedGroup != null) {
                updateItem(group);
                Log.v("updated", "updated");
                return true;
            }
        }
        return false;
    }

    public void addEveryContactChatGroupThatHasMessage() {
        for (Group group : contactChatGroupWithNoMessage.values()) {
            addContactChatGroupIfHasMessage(group);
        }
    }

    @Override
    public Group getItemByKey(Integer key) {
        Group group = contactChatGroupWithNoMessage.get(key);
        if (group != null) {
            return group;
        }

        return super.getItemByKey(key);
    }

    @Override
    public Group removeItemByKey(Integer integer) {
        Group group = contactChatGroupWithNoMessage.remove(integer);
        if (group != null) {
            return group;
        }

        return super.removeItemByKey(integer);
    }
}
