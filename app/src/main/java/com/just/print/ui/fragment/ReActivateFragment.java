package com.just.print.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.just.print.Activate;
import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.ui.activity.LoginActivity;
import com.just.print.ui.holder.ConfigPrintReportViewHolder;
import com.just.print.util.AppUtils;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

public class ReActivateFragment extends BaseFragment implements OnClickItemListener {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_activate;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
    }

    @XClick({R.id.licenseButton})
    void submit(@XGetValueByView(fromId = R.id.license) TextView textView) {
        //check if has internet.
        if(!AppUtils.hasInternet(Applic.app.getApplicationContext())){
            showToast("Please connect the device to internet, then try again.");
            return;
        }

        String inputedSN = String.valueOf(textView.getText());

        if (inputedSN.length() != 6) {
            showToast("Please input correct license code");
            return;
        }

        AppData.setLicense(inputedSN);
        new AppData().start();
    }

    @Override
    public void onClickItem(View view, int i) {

    }
}
