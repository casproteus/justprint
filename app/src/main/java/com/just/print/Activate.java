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
    String[] SerialNumbers = new String[]{
            "asdfas",
            "AFJAIU",
            "RQWEIU",
            "ZXVZXV",
            "IASYDU",
            "WLEJKR",
            "WMENRT",
            "QWKJER",
            "UADSYF",
            "YUSDJF",
            "HJHKQE"
    };

    public static String currentSN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);
        new StupidReflect(this).init();
    }

    @XClick({R.id.licenseButton})
    void submit(@XGetValueByView(fromId = R.id.license) TextView textView) {

        if (textView.getText().length() == 6) {
            String inputedSN = textView.getText().toString().trim();
            for (String sn : SerialNumbers) {
                if (sn.trim().equals(inputedSN)) {
                    AppData.setLicense(this, inputedSN);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }
            }
        }
        showToast("请输入正确的license");
    }

}
