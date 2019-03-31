package com.murphy.pokotalk.adapter.contact;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.PokoListAdapter;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactPokoList;
import com.murphy.pokotalk.view.ContactItem;

public class ContactListAdapter extends PokoListAdapter<Contact> {
    public ContactListAdapter(Context context) {
        super(context);
        setPokoList(new ContactPokoList());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Contact contact = items.get(position);;
        ContactItem item;
        if (convertView == null) {
            item = new ContactItem(context);
            item.inflate();
        } else {
            item = (ContactItem) convertView;
        }
        item.setContact(contact);

        return item;
    }

    @Override
    public Contact getItemFromView(View view) {
        return ((ContactItem) view).getContact();
    }
}
