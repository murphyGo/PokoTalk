package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.view.ListViewDetectable;
import com.murphy.pokotalk.view.MessageItem;

public class MessageListAdapter extends PokoListAdapter<PokoMessage> {
    private boolean bottomAtFirst = true;
    private ListViewDetectable messageListView;

    public MessageListAdapter(Context context) {
        super(context);
        setPokoList(new MessageList());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getMessageId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        PokoMessage message = items.get(position);
        MessageItem item;
        if (convertView == null) {
            item = new MessageItem(context);
            item.inflate();
        } else {
            item = (MessageItem) convertView;
        }
        item.setMessage(message);

        /* Position at bottom at first */
        if (bottomAtFirst && position == getCount() - 1) {
            bottomAtFirst = false;
            if (parent != null && parent instanceof ListViewDetectable) {
                ((ListViewDetectable) parent).postScrollToBottom();
            }
        }

        return item;
    }

    @Override
    public PokoMessage getItemFromView(View view) {
        return ((MessageItem) view).getMessage();
    }

    public void setMessageListView(ListViewDetectable messageListView) {
        this.messageListView = messageListView;
    }
}
