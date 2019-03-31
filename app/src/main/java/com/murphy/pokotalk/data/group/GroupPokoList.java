package com.murphy.pokotalk.data.group;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class GroupPokoList extends ItemPokoList<Integer, Group> {
    public GroupPokoList() {
        super();
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }
}
