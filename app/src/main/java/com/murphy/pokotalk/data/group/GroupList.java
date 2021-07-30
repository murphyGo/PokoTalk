package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class GroupList extends ItemPokoList<Integer, Group> {
    public GroupList() {
        super();
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }
}
