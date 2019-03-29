package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.event.EventListUI;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.view.EventItem;

public class EventListAdapter extends PokoListAdapter<PokoEvent> {
    public EventListAdapter(Context context) {
        super(context);
        setPokoList(new EventListUI());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getEventId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        EventItem item;
        PokoEvent event = items.get(position);
        if (convertView == null) {
            item = new EventItem(context);
            item.inflate();
        } else {
            item = (EventItem) convertView;
        }
        item.setEvent(event);

        return item;
    }

    @Override
    public PokoEvent getItemFromView(View view) {
        return ((EventItem) view).getEvent();
    }
}
