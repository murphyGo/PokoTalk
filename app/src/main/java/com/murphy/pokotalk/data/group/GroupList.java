package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.ItemList;

public class GroupList extends ItemList<Integer, Group> {
    public GroupList() {
        super();
    }

    @Override
    public void updateItem(Group item) {
        super.updateItem(item);

        Group group = getItemByKey(getKey(item));
        if (group == null) {
            add(item);
        } else {
            group.setGroupName(item.getGroupName());
            group.setAlias(item.getAlias());
            group.setNbNewMessages(item.getNbNewMessages());
            group.setMembers(item.getMembers());
        }
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }


}