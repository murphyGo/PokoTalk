package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.MessageList;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.view.ListViewDetectable;
import com.murphy.pokotalk.view.MessageItem;
import com.murphy.pokotalk.view.SpecialMessageItem;
import com.murphy.pokotalk.view.TextMessageItem;

public class MessageListAdapter extends PokoListAdapter<PokoMessage> {
    private boolean bottomAtFirst = true;

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
        MessageItem item = null;
        if (convertView != null) {
            switch (message.getMessageType()) {
                case PokoMessage.TEXT_MESSAGE: {
                    if (convertView instanceof TextMessageItem) {
                        item = (TextMessageItem) convertView;
                    }
                    break;
                }
                case PokoMessage.MEMBER_JOIN:
                case PokoMessage.MEMBER_EXIT: {
                    if (convertView instanceof SpecialMessageItem) {
                        item = (SpecialMessageItem) convertView;
                    }
                    break;
                }
                default:{
                    break;
                }
            }
        }

        if (item == null) {
            switch (message.getMessageType()) {
                case PokoMessage.TEXT_MESSAGE: {
                    item = new TextMessageItem(context);
                    break;
                }
                case PokoMessage.MEMBER_JOIN:
                case PokoMessage.MEMBER_EXIT: {
                    item = new SpecialMessageItem(context);
                    break;
                }
                default:
                    item = new SpecialMessageItem(context);
            }
            item.inflate();
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
        return ((TextMessageItem) view).getMessage();
    }
}
