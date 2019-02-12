package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupListUI;
import com.murphy.pokotalk.view.GroupItem;

public class GroupListAdapter extends PokoListAdapter<Group> {
    public GroupListAdapter(Context context) {
        super(context);
        setPokoList(new GroupListUI());
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getGroupId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        GroupItem item;
        Group group = items.get(position);
        if (convertView == null) {
            item = new GroupItem(context);
            item.inflate();
        } else {
            item = (GroupItem) convertView;
        }
        item.setGroup(group);

        return item;
    }

    @Override
    public Group getItemFromView(View view) {
        return ((GroupItem) view).getGroup();
    }
}
