package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.user.PendingContact;
import com.murphy.pokotalk.data.user.PendingContactList;
import com.murphy.pokotalk.view.PendingContactItem;

public class PendingContactListAdapter extends PokoListAdapter<PendingContact> {
    private boolean invited;

    public PendingContactListAdapter(Context context) {
        super(context);
        setPokoList(new PendingContactList());
        invited = true;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        PendingContact contact = items.get(position);
        PendingContactItem item = new PendingContactItem(context);
        item.inflate();
        item.setNickname(contact.getNickname());
        item.setEmail(contact.getEmail());
        item.setImg(contact.getPicture());
        item.setInvited(invited);
        item.setContact(contact);
        return item;
    }

    @Override
    public void refreshView(View view, PendingContact item) {
        PendingContactItem contactView = (PendingContactItem) view;
        contactView.setNickname(item.getNickname());
        contactView.setImg(item.getPicture());
    }

    @Override
    public PendingContact getItemFromView(View view) {
        return ((PendingContactItem) view).getContact();
    }

    /* This adapter always set invited attribute of views in list to it */
    public void setInvited(boolean invited) {
        this.invited = invited;
    }
}
