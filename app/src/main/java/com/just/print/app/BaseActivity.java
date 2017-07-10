package com.just.print.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.just.print.db.dao.DaoMaster;
import com.just.print.util.ToastUtil;

/**
 * Created by wangx on 2016/10/28.
 */
public class BaseActivity extends FragmentActivity implements EventBus {


    private DaoMaster daoMaster = null;
    final EventBusImpl eventBus = new EventBusImpl();

    final public void post(String eventName, Object... argument) {
        eventBus.post(eventName, argument);
    }


    final public void register(String eventName, EventBusImpl.EventHandler handler) {
        eventBus.register(eventName, handler);
    }


    final public void unregister(String eventName) {
        eventBus.unregister(eventName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daoMaster = Applic.getApp().getDaoMaster();
    }

    public void showToast(String text) {
        ToastUtil.showToast(this, text);
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public FragmentTransaction beginTransaction() {
        return getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
