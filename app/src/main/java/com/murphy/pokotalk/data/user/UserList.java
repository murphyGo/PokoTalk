package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.ItemList;

public class UserList extends ItemList<Integer, User> {
    public UserList() {
        super();
    }

    @Override
    public Integer getKey(User user) {
        return user.getUserId();
    }

    /* If contact not exists, add contact.
       If exists, update contact information. */
    @Override
    public boolean updateItem(User user) {
        super.updateItem(user);

        User exist = getItemByKey(getKey(user));
        if (exist == null) {
            add(user);
            return false;
        } else {
            exist.setNickname(user.getNickname());
            exist.setPicture(user.getPicture());
            return true;
        }
    }
}
