package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.view.PendingContactItem;

import java.util.ArrayList;

public class PendingContactListAdapter extends PokoListAdapter<Contact> {
    private boolean invited;

    public PendingContactListAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, contacts);
        invited = true;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Contact contact = items.get(position);
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
    public Contact getItemFromView(View view) {
        return ((PendingContactItem) view).getContact();
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }
}
