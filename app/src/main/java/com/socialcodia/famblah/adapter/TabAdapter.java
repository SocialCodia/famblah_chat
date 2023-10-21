package com.socialcodia.famblah.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.socialcodia.famblah.fragment.CallsFragment;
import com.socialcodia.famblah.fragment.ChatsFragment;
import com.socialcodia.famblah.fragment.GroupsFragment;
import com.socialcodia.famblah.fragment.StatusFragment;

public class TabAdapter extends FragmentPagerAdapter {

    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public TabAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                StatusFragment statusFragment = new StatusFragment();
                return statusFragment;

            case 2:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                 return "Status";
            case 2:
                return "Groups";
            default:
                return null;
        }
    }
}
