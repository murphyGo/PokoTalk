package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.ItemList;

public class ContactList extends ItemList<Integer, Contact> {
    public ContactList() {
        super();
    }

    @Override
    public Integer getKey(Contact contact) {
        return contact.getUserId();
    }

    /* If contact not exists, add contact.
       If exists, update contact information. */
    @Override
    public boolean updateItem(Contact contact) {
        super.updateItem(contact);

        Contact exist = getItemByKey(getKey(contact));
        if (exist == null) {
            add(contact);
            return false;
        } else {
            exist.setNickname(contact.getNickname());
            exist.setPicture(contact.getPicture());
            exist.setLastSeen(contact.getLastSeen());
            return true;
        }
    }
}
