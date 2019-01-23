package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.ItemList;

public class StrangerList extends ItemList<Integer, Stranger> {
    public StrangerList() {
        super();
    }

    @Override
    public Integer getKey(Stranger user) {
        return user.getUserId();
    }

    /* If contact not exists, add contact.
       If exists, update contact information. */
    @Override
    public void updateItem(Stranger user) {
        super.updateItem(user);

        Stranger exist = getItemByKey(getKey(user));
        if (exist == null) {
            add(user);
        } else {
            exist.setNickname(user.getNickname());
            exist.setPicture(user.getPicture());
        }
    }
}
