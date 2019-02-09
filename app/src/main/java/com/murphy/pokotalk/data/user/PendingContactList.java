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

    /* If contact not exists, add contact.
       If exists, update contact information. */
    @Override
    public boolean updateItem(PendingContact contact) {
        super.updateItem(contact);

        PendingContact exist = getItemByKey(getKey(contact));
        if (exist == null) {
            add(contact);
            return false;
        } else {
            exist.setNickname(contact.getNickname());
            exist.setPicture(contact.getPicture());
            return true;
        }
    }
}
