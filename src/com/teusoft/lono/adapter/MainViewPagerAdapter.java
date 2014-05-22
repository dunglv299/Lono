package com.teusoft.lono.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import fragment.MainViewFragment;

/**
 * Created by DungLV on 22/5/2014.
 */
public class MainViewPagerAdapter extends FragmentPagerAdapter {
    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MainViewFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 3;
    }
}
