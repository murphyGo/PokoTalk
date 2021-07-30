package com.murphy.pokotalk.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.murphy.pokotalk.R;

public class PendingContactListViewPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private Fragment[] fragments;

    public PendingContactListViewPagerAdapter(Context context,
                                              FragmentManager fragmentManager, Fragment[] fragments) {
        super(fragmentManager);
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: {
                return context.getString(R.string.pending_contact_list_invited);
            }
            case 1: {
                return context.getString(R.string.pending_contact_list_inviting);
            }
            default: {
                return null;
            }
        }
    }
}
