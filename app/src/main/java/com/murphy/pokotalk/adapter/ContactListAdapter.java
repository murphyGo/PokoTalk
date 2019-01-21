package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.murphy.pokotalk.data.Contact;
import com.murphy.pokotalk.view.ContactItem;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter {
    private ArrayList<Contact> contacts;
    private Context context;

    public ContactListAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contacts.get(position).getUserId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = contacts.get(position);
        ContactItem item = new ContactItem(context);
        item.setNickname(contact.getNickname());
        item.setEmail(contact.getEmail());
        item.setImg(contact.getPicture());
        item.inflate();
        item.setOnClickListener(itemClickListener);
        return item;
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
