package com.teusoft.lono.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import fragment.DayGraphFragment;

import java.util.ArrayList;

/**
 * Created by DungLV on 20/4/2014.
 */
public class DayGraphPagerAdapter extends FragmentPagerAdapter {
    private int pageCount;
    private int channel;
    private long roundStartDate;
    private boolean isDegreeF;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private FragmentManager fragmentManager;

    public DayGraphPagerAdapter(FragmentManager fm, int channel, int pageCount, long roundStartDate, boolean isDegreeF) {
        super(fm);
        this.pageCount = pageCount;
        this.channel = channel;
        this.roundStartDate = roundStartDate;
        this.isDegreeF = isDegreeF;
        this.fragmentManager = fm;
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = DayGraphFragment.create(position, channel, roundStartDate, isDegreeF);
        fragments.add(fragment);
        return fragment;
    }

    public void clearAll() {
        for (int i = 0; i < fragments.size(); i++)
            fragmentManager.beginTransaction().remove(fragments.get(i)).commit();
        fragments.clear();
    }

    @Override
    public int getCount() {
        return pageCount;
    }
}
