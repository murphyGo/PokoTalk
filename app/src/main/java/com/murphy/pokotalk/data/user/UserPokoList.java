package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class UserPokoList extends ItemPokoList<Integer, User> {
    public UserPokoList() {
        super();
    }

    @Override
    public Integer getKey(User user) {
        return user.getUserId();
    }
}
