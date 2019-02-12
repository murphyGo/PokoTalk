package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.ItemList;

public class GroupList extends ItemList<Integer, Group> {
    public GroupList() {
        super();
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }
}
