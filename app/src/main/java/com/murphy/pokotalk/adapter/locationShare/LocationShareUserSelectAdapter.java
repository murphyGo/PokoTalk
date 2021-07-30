package com.murphy.pokotalk.adapter.locationShare;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.activity.chat.UserPosition;
import com.murphy.pokotalk.adapter.PokoAdapter;
import com.murphy.pokotalk.view.LocationShareUserSelectItem;
import com.naver.maps.map.overlay.Marker;

import java.util.List;

public class LocationShareUserSelectAdapter extends PokoAdapter<UserPosition> {
    private List<UserPosition> positions;

    public LocationShareUserSelectAdapter(Context context,
                                          List<UserPosition> positions) {
        super(context);
        this.positions = positions;
    }

    @Override
    public int getCount() {
        return positions.size();
    }

    @Override
    public UserPosition getItem(int position) {
        return positions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        LocationShareUserSelectItem itemView = null;

        if (convertView instanceof LocationShareUserSelectItem) {
            itemView = (LocationShareUserSelectItem) convertView;
        }

        if (itemView == null) {
            itemView = new LocationShareUserSelectItem(context);
            itemView.inflate();
        }

        // Get item
        UserPosition item = getItem(position);

        // Get marker
        Marker marker = item.getMarker();

        if (marker != null) {
            itemView.setName(marker.getCaptionText());
            itemView.setImg(marker.getIcon().getBitmap(context));
        }

        return itemView;
    }

    @Override
    public UserPosition getItemFromView(View view) {
        return null;
    }
}
