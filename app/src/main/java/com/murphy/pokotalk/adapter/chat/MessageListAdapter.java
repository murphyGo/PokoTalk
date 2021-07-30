package com.murphy.pokotalk.adapter.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.DateChangeListAdapter;
import com.murphy.pokotalk.data.extra.DateChangeItem;
import com.murphy.pokotalk.data.group.MessageListUI;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.view.DateChangeMessageItem;
import com.murphy.pokotalk.view.FileShareMessageItem;
import com.murphy.pokotalk.view.ImageMessageItem;
import com.murphy.pokotalk.view.MessageItem;
import com.murphy.pokotalk.view.SpecialMessageItem;
import com.murphy.pokotalk.view.TextMessageItem;

public class MessageListAdapter extends DateChangeListAdapter<PokoMessage> {
    public MessageListAdapter(Context context) {
        super(context);
        setPokoList(new MessageListUI());
    }
/*
    @Override
    public long getItemId(int position) {
        return ((MessageListUI) pokoList).getKey(items.get(position));
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
        // Get message
        PokoMessage message = (PokoMessage) items.get(position);
        MessageItem item = null;

        // Recycle convert view if it is type of message view we need
        if (convertView != null) {
            switch (message.getMessageType()) {
                case PokoMessage.TYPE_TEXT_MESSAGE: {
                    if (convertView instanceof TextMessageItem) {
                        item = (TextMessageItem) convertView;
                    }
                    break;
                }
                case PokoMessage.TYPE_IMAGE: {
                    if (convertView instanceof ImageMessageItem) {
                        item = (ImageMessageItem) convertView;
                    }
                    break;
                }
                case PokoMessage.TYPE_FILE_SHARE: {
                    if (convertView instanceof FileShareMessageItem) {
                        item = (FileShareMessageItem) convertView;
                    }
                    break;
                }
                case PokoMessage.TYPE_MEMBER_JOIN:
                case PokoMessage.TYPE_MEMBER_EXIT: {
                    if (convertView instanceof SpecialMessageItem) {
                        item = (SpecialMessageItem) convertView;
                    }
                    break;
                }
                case PokoMessage.TYPE_APP_DATE_MESSAGE: {
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

        // Create new message view if we can not recycle convert view
        if (item == null) {
            switch (message.getMessageType()) {
                case PokoMessage.TYPE_TEXT_MESSAGE: {
                    item = new TextMessageItem(context);
                    break;
                }
                case PokoMessage.TYPE_IMAGE: {
                    item = new ImageMessageItem(context);
                    break;
                }
                case PokoMessage.TYPE_FILE_SHARE: {
                    item = new FileShareMessageItem(context);
                    break;
                }
                case PokoMessage.TYPE_MEMBER_JOIN:
                case PokoMessage.TYPE_MEMBER_EXIT: {
                    item = new SpecialMessageItem(context);
                    break;
                }
                case PokoMessage.TYPE_APP_DATE_MESSAGE: {
                    item = new DateChangeMessageItem(context);
                    break;
                }
                default:
                    item = new SpecialMessageItem(context);
            }
            item.inflate();
        }

        // Set message
        item.setMessage(message);

        return item;
    }

    @Override
    public PokoMessage getItemFromView(View view) {
        return ((TextMessageItem) view).getMessage();
    }
}
