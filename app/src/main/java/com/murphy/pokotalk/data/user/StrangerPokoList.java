package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class StrangerPokoList extends ItemPokoList<Integer, Stranger> {
    public StrangerPokoList() {
        super();
    }

    @Override
    public Integer getKey(Stranger user) {
        return user.getUserId();
    }
}
