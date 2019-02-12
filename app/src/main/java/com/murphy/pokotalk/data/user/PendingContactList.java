package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.ItemList;

public class PendingContactList extends ItemList<Integer, PendingContact> {
    public PendingContactList() {
        super();
    }

    @Override
    public Integer getKey(PendingContact contact) {
        return contact.getUserId();
    }
}
