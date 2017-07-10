package com.just.print.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseActivity;
import com.just.print.ui.adapter.MyPagerAdapter;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.just.print.ui.fragment.OrderMenuFragment;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends BaseActivity {
    @XViewByID(R.id.pager)
    private ViewPager viewPager;  //对应的viewPager
    private PagerAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity);
        new StupidReflect(this).init();
        //List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        List<Fragment> fragmentList = new ArrayList<Fragment>();
        fragmentList.add(new OrderCategoryFragment());
        //fragmentList.add(new OrderMenuFragment());
        fragmentList.add(new OrderIdentifierFragment());
        adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList);
        try {
            if (viewPager != null) {
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    @XClick({R.id.gotoConfig})
    private void gotoConfig() {
        startActivity(new Intent(this, ConfigActivity.class));
    }
*/
}
