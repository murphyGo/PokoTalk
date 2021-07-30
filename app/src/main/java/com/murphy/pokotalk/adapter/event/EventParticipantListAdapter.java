package com.murphy.pokotalk.adapter.event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.PokoListAdapter;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.data.user.UserList;
import com.murphy.pokotalk.view.ChatMemberItem;

public class EventParticipantListAdapter extends PokoListAdapter<User> {
    public EventParticipantListAdapter(Context context) {
        super(context);
        setPokoList(new UserList());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUserId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        User member = items.get(position);
        ChatMemberItem item;
        if (convertView == null) {
            item = new ChatMemberItem(context);
            item.inflate();
        } else {
            item = (ChatMemberItem) convertView;
        }
        item.setUser(member);

        return item;
    }

    @Override
    public User getItemFromView(View view) {
        return ((ChatMemberItem) view).getUser();
    }
}