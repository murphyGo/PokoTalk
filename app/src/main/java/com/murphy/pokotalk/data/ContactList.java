package com.murphy.pokotalk.data;

import java.util.ArrayList;

public class ContactList extends List<Integer, Contact> {
    public ContactList() {
        super();
    }

    @Override
    public Integer getKey(Contact contact) {
        return contact.getUserId();
    }

    /* If contact not exists, add contact.
       If exists, update contact information. */
    public void updateContact(Contact contact) {
        Contact exist = getContactById(getKey(contact));
        if (exist == null) {
            addContact(contact);
        } else {
            exist.setNickname(contact.getNickname());
            exist.setGroupId(contact.getGroupId());
            exist.setPicture(contact.getPicture());
            exist.setLastSeen(contact.getLastSeen());
            exist.setContactId(contact.getContactId());
        }
    }

    public boolean addContact(Contact contact) {
        return super.add(contact);
    }

    public boolean removeContactById(int i) {
        return super.remove(i);
    }

    public Contact getContactById(int i) {
        return hashMap.get(i);
    }

    public ArrayList<Contact> getContactList() {
        return arrayList;
    }

    public void updateContactList() {

    }
}
