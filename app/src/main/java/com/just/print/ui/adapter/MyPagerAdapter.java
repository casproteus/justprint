package com.just.print.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiqi on 2016/11/6.
 */

public class MyPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<Fragment>();

    public MyPagerAdapter(FragmentManager fm){
        super(fm);
    }
    public MyPagerAdapter(FragmentManager fragmentManager,
                          List<Fragment> fragmentList){
        super(fragmentManager);
        this.fragmentList = fragmentList;
    }
    @Override
    public Fragment getItem(int index){
        return fragmentList.get(index);
    }
    @Override
    public int getCount(){
        return fragmentList.size();
    }

}
