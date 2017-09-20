package com.just.print_night.ui.fragment;


import android.os.Bundle;
import android.view.View;

import com.just.print_night.R;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.ui.activity.ConfigActivity;
import com.just.print_night.util.DatabaseUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;


public class ConfigMainFragment extends BaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.config_main_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    @XClick({R.id.configMenuManager, R.id.configPrintManager, R.id.configTagManager,
            R.id.configUserManager, R.id.configPrintReport, R.id.synchronizedb, R.id.reactivate})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.configMenuManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigMenuFragment.class);
                break;
            case R.id.configUserManager:
                // getEventBus().post(ConfigActivity.CHANGE_PAGE, Config.class);
                break;
            case R.id.configTagManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigMarkFragment.class);
                break;
            case R.id.configPrintManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigPrinterFragment.class);
                break;
            case R.id.configPrintReport:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigPrintReportFragment.class);
                break;
            case R.id.synchronizedb:
                DatabaseUtil.syncDbOntoServer();
                break;
            case R.id.reactivate:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ReActivateFragment.class);
                break;
        }
    }
}
