package com.teusoft.lono.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import fragment.DayGraphFragment;

/**
 * Created by DungLV on 20/4/2014.
 */
public class DayGraphPagerAdapter extends FragmentStatePagerAdapter {
    private int pageCount;
    private int channel;
    private long roundStartDate;
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private boolean isDegreeF;


    public DayGraphPagerAdapter(FragmentManager fm, int channel, int pageCount, long roundStartDate, boolean isDegreeF) {
        super(fm);
        this.pageCount = pageCount;
        this.channel = channel;
        this.roundStartDate = roundStartDate;
        this.isDegreeF = isDegreeF;
    }


    @Override
    public Fragment getItem(int position) {
        return DayGraphFragment.create(position, channel, roundStartDate, isDegreeF);
    }


    @Override
    public int getCount() {
        return pageCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}
