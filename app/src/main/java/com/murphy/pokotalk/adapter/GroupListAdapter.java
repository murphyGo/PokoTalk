package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.view.GroupItem;

import java.util.ArrayList;

public class GroupListAdapter extends PokoListAdapter<Group> {
    public GroupListAdapter(Context context, ArrayList<Group> groups) {
        super(context, groups);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getGroupId();
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        Group group = items.get(position);
        GroupItem item = new GroupItem(context);
        item.inflate();
        item.setGroup(group);
        return item;
    }

    @Override
    public void refreshView(View view, Group item) {
        GroupItem groupView = (GroupItem) view;
        groupView.setGroup(item);
    }

    @Override
    public Group getItemFromView(View view) {
        return ((GroupItem) view).getGroup();
    }
}
