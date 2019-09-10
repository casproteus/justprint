package com.just.print_night;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseActivity;
import com.just.print_night.ui.activity.LoginActivity;
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
        AppData.setLicense(inputedSN, textView);

        while(true) {
            //when inputted activate code, should not go to login activity(that activaty will check name and store name then go to Main Interface)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String number = AppData.getCustomData("number");
            if(Long.valueOf(number) > 0){
                startActivity(new Intent(this, LoginActivity.class));
                break;
            }
        }
    }

}
