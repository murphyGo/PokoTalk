package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.view.MessageItem;

public class MessageListAdapter extends PokoListAdapter<Message> {
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
        Message message = items.get(position);
        MessageItem item;
        if (convertView == null) {
            item = new MessageItem(context);
            item.inflate();
        } else {
            item = (MessageItem) convertView;
        }
        item.setMessage(message);

        return item;
    }

    @Override
    public Message getItemFromView(View view) {
        return ((MessageItem) view).getMessage();
    }
}
