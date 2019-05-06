package com.murphy.pokotalk.adapter.contact;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.PokoListAdapter;
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
        PendingContact pendingContact = items.get(position);
        PendingContactItem item;
        if (convertView == null) {
            item = new PendingContactItem(context);
            item.inflate();
        } else {
            item = (PendingContactItem) convertView;
        }
        item.setPendingContact(pendingContact);
        item.setInvited(invited);

        return item;
    }

    @Override
    public PendingContact getItemFromView(View view) {
        return ((PendingContactItem) view).getPendingContact();
    }

    /* This adapter always set invited attribute of views in list to it */
    public void setInvited(boolean invited) {
        this.invited = invited;
    }
}
