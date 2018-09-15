package com.example.wqllj.locationshare.view;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by cloud on 2018/8/29.
 */

public class MainContentAdatoer extends FragmentPagerAdapter {
    private List<Fragment> mlist;
    private String[] titles = new String[]{"消息","足迹","互动"};
    public MainContentAdatoer(FragmentManager fm, List<Fragment> mlist) {
        super(fm);
        this.mlist=mlist;
    }

    @Override
    public Fragment getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public int getCount() {
        return mlist==null?0:mlist.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
