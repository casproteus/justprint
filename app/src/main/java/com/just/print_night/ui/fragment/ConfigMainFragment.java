package com.just.print_night.ui.fragment;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.ui.activity.ConfigActivity;
import com.just.print_night.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;


public class ConfigMainFragment extends BaseFragment {

    @XViewByID(R.id.password)
    EditText password;

    @Override
    protected int getLayoutId() {
        return R.layout.config_main_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    @XClick({R.id.verifyPassword})
    private void onConfirmUserName(View view) {
        String inputContent = this.password.getText().toString().trim();
        if (StringUtils.isEmpty(inputContent) || inputContent.length() < 2) {
            showToast("Please input the right password");
        } else {
            String userPassword = AppData.getCustomData("userPassword");
            if(userPassword == null || userPassword.length() == 0){
                userPassword = AppData.getLicense();
            }

            if(!userPassword.equals(inputContent)){
                showToast("Please input the right password");
                return;
            }

            findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.configList).setVisibility(View.VISIBLE);
        }
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
                getEventBus().post(ConfigActivity.CHANGE_PAGE, SynchronizeDBFragment.class);
                break;
            case R.id.reactivate:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ReActivateFragment.class);
                break;
        }
    }
}
