package com.example.artem.blogapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabAdapter extends FragmentPagerAdapter {

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                PostFragment requestsFragment = new PostFragment();
                return requestsFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Requests";
            case 1:
                return "Chat";
            case 2:
                return "Friends";
            default:
                return null;
        }
    }
}
