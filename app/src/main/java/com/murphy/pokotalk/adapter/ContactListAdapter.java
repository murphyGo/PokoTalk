package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.view.ContactItem;

import java.util.ArrayList;

public class ContactListAdapter extends PokoListAdapter<Contact> {
    public ContactListAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, contacts);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Contact contact = items.get(position);
        ContactItem item = new ContactItem(context);;
        item.inflate();
        item.setContact(contact);
        return item;
    }

    @Override
    public void refreshView(View view, Contact item) {
        ContactItem contactView = (ContactItem) view;
        contactView.setContact(item);
    }

    @Override
    public Contact getItemFromView(View view) {
        return ((ContactItem) view).getContact();
    }
}
