package com.just.print_night.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.ui.activity.MainActivity;
import com.just.print_night.ui.activity.OrderActivity;
import com.just.print_night.util.AppUtils;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;

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

        String inputedSN = String.valueOf(textView.getText()).trim();
        if(inputedSN.startsWith("-") && inputedSN.indexOf(":") > 1){
            int p = inputedSN.indexOf(":");
            String SettingType = inputedSN.substring(1,p);
            inputedSN = inputedSN.substring(p + 1);
            switch (SettingType) {
                case "debug":
                    MainActivity.debug = Boolean.valueOf(inputedSN);
                    AppData.putCustomData("debug", String.valueOf(MainActivity.debug));
                    if(MainActivity.debug) {
                        showToast("App is in debug mode, when bug appears again, please write down the system time and report.");

                        StringBuilder sb = new StringBuilder();
                        sb.append("adminPassword:").append(AppData.getCustomData("adminPassword")).append("\n");
                        sb.append("debug mode:").append(AppData.getCustomData("debug")).append("\n");
                        sb.append("lastsuccess:").append(AppData.getCustomData("lastsuccess")).append("\n");
                        sb.append("limitation:").append(AppData.getCustomData("limitation")).append("\n");
                        sb.append("mode:").append(AppData.getCustomData("mode")).append("\n");
                        sb.append("number:").append(AppData.getCustomData("number")).append("\n");
                        sb.append("reportStartDate:").append(AppData.getCustomData("reportStartDate")).append("\n");
                        sb.append("userPassword:").append(AppData.getCustomData("userPassword")).append("\n");
                        sb.append("version:").append(AppData.getCustomData("version")).append("\n");
                        textView.setText(sb.toString());
                    }else{
                        showToast("debug mode turned off!");
                    }
                    return;
                case "v":
                    AppData.putCustomData("version", inputedSN);
                    showToast("App is in switched to version : " + inputedSN);
                    return;
                case "m":
                    AppData.putCustomData("mode", inputedSN);
                    showToast("App is in switched to mode : " + inputedSN);
                    return;
                case "l":
                    AppData.putCustomData("limitation", inputedSN);
                    showToast("App has switched limitation to: " + inputedSN);
                    return;
                case "f":
                    AppData.putCustomData("font", inputedSN);
                    showToast("App has set font to: " + inputedSN);
                    return;
                case "w":
                    AppData.putCustomData("width", inputedSN);
                    if("16".equals(inputedSN)){
                        AppData.putCustomData("font", "29, 33, 34");
                    }
                    showToast("App has set width to: " + inputedSN);
                    return;
                case "c":
                    AppData.putCustomData("code", inputedSN);
                    showToast("App has set code to: " + inputedSN);
                    return;
                case "p":   // means theres parameters
                    AppData.putCustomData("adminPassword", inputedSN);
                    showToast("App has set adminPassword to: " + inputedSN);
                    return;
                case "s1":
                    AppData.putCustomData("sep_str1", inputedSN);
                    showToast("App has set sep_str1 to: " + inputedSN);
                    return;
                case "s2":
                    AppData.putCustomData("sep_str2", inputedSN);
                    showToast("App has set sep_str2 to: " + inputedSN);
                    return;
                case "lastchar":
                    AppData.putCustomData(AppData.KEY_CUST_LAST_CHAR, inputedSN);
                    showToast("Please restart app to apply new layout.");
                    return;
                default:
                    AppData.putCustomData(SettingType, inputedSN);
                    showToast(SettingType + " is set to " + inputedSN);
            }
        }else if (inputedSN.length() != 6) {
            showToast("Please input correct license code");
            return;
        }

        AppData.setLicense(inputedSN);
        new AppData().start();

        startActivity(new Intent(getContext(), OrderActivity.class));
        getActivity().finish();
    }

    @Override
    public void onClickItem(View view, int i) {

    }
}
