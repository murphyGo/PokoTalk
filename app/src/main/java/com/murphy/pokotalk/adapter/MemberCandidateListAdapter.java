package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.view.MemberCandidateItem;

import java.util.ArrayList;

public class MemberCandidateListAdapter extends PokoListAdapter<Contact> {
    public MemberCandidateListAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, contacts);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Contact contact = items.get(position);
        MemberCandidateItem item = new MemberCandidateItem(context);
        item.inflate();
        item.setContact(contact);
        return item;
    }

    @Override
    public void refreshView(View view, Contact item) {
        MemberCandidateItem contactView = (MemberCandidateItem) view;
        contactView.setContact(item);
    }

    @Override
    public Contact getItemFromView(View view) {
        return ((MemberCandidateItem) view).getContact();
    }
}
