package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.list.ItemPokoList;

public class PendingContactList extends ItemPokoList<Integer, PendingContact> {
    public PendingContactList() {
        super();
    }

    @Override
    public Integer getKey(PendingContact contact) {
        return contact.getUserId();
    }
}
