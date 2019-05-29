package com.just.print_night;

import android.os.Bundle;
import android.widget.TextView;

import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseActivity;
import com.just.print_night.util.AppUtils;
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

        int p = inputedSN.indexOf("-v:");
        if(p > 0){
            AppData.putCustomData("version", inputedSN.substring(p+3).trim());
            inputedSN = inputedSN.substring(0,p).trim();
        }


        if (inputedSN.length() != 6) {
            showToast("Please input correct license code");
            return;
        }

        AppData.setLicense(inputedSN);
        new AppData().start();
        //startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
