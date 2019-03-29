package com.murphy.pokotalk.view;

import android.content.Context;
import android.widget.FrameLayout;

import com.murphy.pokotalk.data.event.PokoEvent;

public class EventItem extends FrameLayout {
    private Context context;
    private PokoEvent event;

    public EventItem(Context context) {
        super(context);
        this.context = context;
    }

    public void inflate() {

    }

    public PokoEvent getEvent() {
        return event;
    }

    public void setEvent(PokoEvent event) {
        this.event = event;
    }
}
