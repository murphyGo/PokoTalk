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
}
