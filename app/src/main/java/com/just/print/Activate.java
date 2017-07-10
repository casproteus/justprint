package com.just.print;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.just.print.app.AppData;
import com.just.print.app.BaseActivity;
import com.just.print.ui.activity.LoginActivity;
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

        if (textView.getText().length() == 6) {
            AppData.setLicense(this, textView.getText().toString());
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            showToast("请输入正确的license");
        }

    }

}
