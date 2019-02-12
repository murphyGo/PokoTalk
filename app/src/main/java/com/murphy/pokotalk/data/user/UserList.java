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
}
