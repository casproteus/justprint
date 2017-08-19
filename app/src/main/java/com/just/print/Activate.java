package com.just.print;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseActivity;
import com.just.print.ui.activity.LoginActivity;
import com.just.print.util.AppUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;

public class Activate extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);
        new StupidReflect(this).init();
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
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
