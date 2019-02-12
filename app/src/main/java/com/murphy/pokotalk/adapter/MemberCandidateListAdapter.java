package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.data.user.ContactList;
import com.murphy.pokotalk.view.MemberCandidateItem;

public class MemberCandidateListAdapter extends PokoListAdapter<Contact> {
    public MemberCandidateListAdapter(Context context) {
        super(context);
        setPokoList(new ContactList());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Contact contact = items.get(position);
        MemberCandidateItem item;
        if (convertView == null) {
            item = new MemberCandidateItem(context);
            item.inflate();
        } else {
            item = (MemberCandidateItem) convertView;
        }
        item.setContact(contact);

        return item;
    }

    @Override
    public Contact getItemFromView(View view) {
        return ((MemberCandidateItem) view).getContact();
    }
}
