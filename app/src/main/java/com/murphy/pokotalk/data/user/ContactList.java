package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.ItemList;

import java.util.HashMap;

public class ContactList extends ItemList<Integer, Contact> {
    protected HashMap<Integer, ContactGroupRelation> contactChatMapUserId;
    protected HashMap<Integer, ContactGroupRelation> contactChatMapGroupId;

    public ContactList() {
        super();
        contactChatMapUserId = new HashMap<>();
        contactChatMapGroupId = new HashMap<>();
    }

    @Override
    public Integer getKey(Contact contact) {
        return contact.getUserId();
    }

    public void putContactGroupRelation(int contactUserId, int groupId) {
        ContactGroupRelation relation = new ContactGroupRelation();
        relation.groupId = groupId;
        relation.contactUserId = contactUserId;
        contactChatMapUserId.put(contactUserId, relation);
        contactChatMapGroupId.put(groupId, relation);
    }

    public ContactGroupRelation getContactGroupRelationByUserId(int contactUserId) {
        return contactChatMapUserId.get(contactUserId);
    }

    public ContactGroupRelation getContactGroupRelationByGroupId(int groupId) {
        return contactChatMapGroupId.get(groupId);
    }

    public ContactGroupRelation removeContactGroupRelationByUserId(int contactUserId) {
        ContactGroupRelation relation = contactChatMapUserId.remove(contactUserId);
        if (relation == null)
            return null;
        contactChatMapGroupId.remove(relation.getGroupId());
        return relation;
    }

    public ContactGroupRelation removeContactGroupRelationByGroupId(int groupId) {
        ContactGroupRelation relation = contactChatMapGroupId.remove(groupId);
        if (relation == null)
            return null;
        contactChatMapUserId.remove(relation.contactUserId);
        return relation;
    }

    public class ContactGroupRelation {
        protected int contactUserId;
        protected int groupId;

        public int getContactUserId() {
            return contactUserId;
        }

        public void setContactUserId(int contactUserId) {
            this.contactUserId = contactUserId;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
    }
}
