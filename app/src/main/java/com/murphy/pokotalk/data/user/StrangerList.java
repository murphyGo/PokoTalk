package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class StrangerList extends ItemPokoList<Integer, Stranger> {
    public StrangerList() {
        super();
    }

    @Override
    public Integer getKey(Stranger user) {
        return user.getUserId();
    }
}
