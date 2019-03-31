package com.murphy.pokotalk.adapter.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.DateChangeListAdapter;
import com.murphy.pokotalk.data.extra.DateChangeItem;
import com.murphy.pokotalk.data.group.MessagePokoListUI;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.view.DateChangeMessageItem;
import com.murphy.pokotalk.view.ListViewDetectable;
import com.murphy.pokotalk.view.MessageItem;
import com.murphy.pokotalk.view.SpecialMessageItem;
import com.murphy.pokotalk.view.TextMessageItem;

public class MessageListAdapter extends DateChangeListAdapter<PokoMessage> {
    private boolean bottomAtFirst = true;

    public MessageListAdapter(Context context) {
        super(context);
        setPokoList(new MessagePokoListUI());
    }
/*
    @Override
    public long getItemId(int position) {
        return ((MessagePokoListUI) pokoList).getKey(items.get(position));
    }
*/
    @Override
    public View createDateChangeView(DateChangeItem item, View convertView, ViewGroup parent) {
        DateChangeMessageItem dateChangeMessageItem = null;
        if (convertView != null) {
            if (convertView instanceof DateChangeMessageItem) {
                dateChangeMessageItem = (DateChangeMessageItem) convertView;
            }
        }

        if (dateChangeMessageItem == null) {
            dateChangeMessageItem = new DateChangeMessageItem(context);
            dateChangeMessageItem.inflate();
        }

        dateChangeMessageItem.setDateChangeItem(item);

        return dateChangeMessageItem;
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        PokoMessage message = (PokoMessage) items.get(position);
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
                case PokoMessage.APP_DATE_MESSAGE: {
                    if (convertView instanceof DateChangeMessageItem) {
                        item = (DateChangeMessageItem) convertView;
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
                case PokoMessage.APP_DATE_MESSAGE: {
                    item = new DateChangeMessageItem(context);
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
