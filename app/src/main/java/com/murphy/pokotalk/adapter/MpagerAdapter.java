package com.murphy.pokotalk.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.murphy.pokotalk.R;

/* MainActivity's slider screen adapter */
public class MpagerAdapter extends PagerAdapter {
    private int[] layouts;
    private View[] views;
    private LayoutInflater inflater;
    private Context context;

    public MpagerAdapter(Context context, int[] layouts) {
        this.layouts = layouts;
        this.context = context;
        this.views = new View[layouts.length];
        inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(layouts[position], container, false);
        container.addView(view);
        views[position] = view;

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /* Find element view by its position in layouts, looks like findViewById method */
    public <T extends View> T findViewByPosition(int position) {
        return (T) views[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: {
                return context.getString(R.string.tab_contact_list);
            }
            case 1: {
                return context.getString(R.string.tab_group_list);
            }
            case 2: {
                return context.getString(R.string.tab_event_list);
            }
            case 3: {
                return context.getString(R.string.tab_settings);
            }
            default: {
                return null;
            }
        }
    }


}
