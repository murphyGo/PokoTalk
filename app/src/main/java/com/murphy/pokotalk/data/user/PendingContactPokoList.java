package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class PendingContactPokoList extends ItemPokoList<Integer, PendingContact> {
    public PendingContactPokoList() {
        super();
    }

    @Override
    public Integer getKey(PendingContact contact) {
        return contact.getUserId();
    }
}
