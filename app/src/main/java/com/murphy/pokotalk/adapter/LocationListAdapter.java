package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.event.LocationSearchResult;

import java.util.ArrayList;

public class LocationListAdapter extends BaseAdapter {
    protected ArrayList<LocationSearchResult> list;
    protected Context context;
    protected ViewCreationCallback callback;

    public LocationListAdapter(Context context) {
        this.context = context;
    }

    public void setLocations(ArrayList<LocationSearchResult> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationSearchResult item = list.get(position);
        String title = item.getTitle();
        String category = item.getCategory();
        String address = item.getAddress();

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.location_search_item, parent, false);
        }

        // Find views
        TextView titleView = convertView.findViewById(R.id.locationSearchTitle);
        TextView categoryView = convertView.findViewById(R.id.locationSearchCategory);
        TextView addressView = convertView.findViewById(R.id.locationSearchAddress);

        // Transfer data to views
        titleView.setText(title);
        categoryView.setText(category);
        addressView.setText(address);

        // Run user callback
        if (callback != null) {
            callback.run(convertView, item);
        }

        return convertView;
    }

    public void setViewCreationCallback(ViewCreationCallback callback) {
        this.callback = callback;
    }
}
