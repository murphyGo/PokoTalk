package com.murphy.pokotalk.adapter.event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.adapter.DateChangeListAdapter;
import com.murphy.pokotalk.data.event.EventListUI;
import com.murphy.pokotalk.data.event.PokoEvent;
import com.murphy.pokotalk.data.extra.DateChangeItem;
import com.murphy.pokotalk.view.DateChangeMessageItem;
import com.murphy.pokotalk.view.EventItem;

public class EventListAdapter extends DateChangeListAdapter<PokoEvent> {
    public EventListAdapter(Context context) {
        super(context);
        setPokoList(new EventListUI());
    }

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
        EventItem item = null;
        PokoEvent event = (PokoEvent) items.get(position);
        if (convertView != null) {
            if (convertView instanceof EventItem) {
                item = (EventItem) convertView;
            }
        }

        if (item == null) {
            item = new EventItem(context);
            item.inflate();
        }

        item.setEvent(event);

        return item;
    }

    @Override
    public PokoEvent getItemFromView(View view) {
        return ((EventItem) view).getEvent();
    }
}
