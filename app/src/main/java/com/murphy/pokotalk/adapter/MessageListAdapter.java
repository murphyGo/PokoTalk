package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.Message;
import com.murphy.pokotalk.view.MessageItem;

import java.util.ArrayList;

public class MessageListAdapter extends PokoListAdapter<Message> {
    public MessageListAdapter(Context context, ArrayList<Message> messages) {
        super(context, messages);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getMessageId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Message message = items.get(position);
        MessageItem item = new MessageItem(context);;
        item.inflate();
        item.setMessage(message);
        return item;
    }

    @Override
    public void refreshView(View view, Message item) {
        MessageItem messageView = (MessageItem) view;
        messageView.setMessage(item);
    }

    public void refreshViewsNbNotReadUser(int fromId, int toId) {
        if (fromId > toId)
            return;

        for (int i = fromId; i <= toId; i++) {
            MessageItem messageView = (MessageItem) getView(i);
            if (messageView == null)
                continue;
            Message message = getItemFromView(messageView);
            messageView.setNbNotReadUser(message.getNbNotReadUser());
        }
    }

    @Override
    public Message getItemFromView(View view) {
        return ((MessageItem) view).getMessage();
    }
}
