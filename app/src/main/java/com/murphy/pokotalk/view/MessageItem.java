package com.murphy.pokotalk.view;

import android.content.Context;
import android.widget.FrameLayout;

import com.murphy.pokotalk.data.group.PokoMessage;

public abstract class MessageItem extends FrameLayout {
    protected Context context;
    protected PokoMessage message;

    public MessageItem(Context context) {
        super(context);
        this.context = context;
    }

    public PokoMessage getMessage() {
        return message;
    }

    public abstract void inflate();
    public abstract void setMessage(PokoMessage message);
}
