package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class UserList extends ItemPokoList<Integer, User> {
    public UserList() {
        super();
    }

    @Override
    public Integer getKey(User user) {
        return user.getUserId();
    }
}
