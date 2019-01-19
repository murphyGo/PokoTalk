package com.murphy.pokotalk.data;

import java.util.ArrayList;

public class GroupList extends List<Integer, Group> {
    public GroupList() {
        super();
    }

    @Override
    public Integer getKey(Group group) {
        return group.getGroupId();
    }

    public boolean addGroup(Group contact) {
        return super.add(contact);
    }

    public boolean removeGroupById(int i) {
        return super.remove(i);
    }

    public ArrayList<Group> getGroupList() {
        return arrayList;
    }
}
