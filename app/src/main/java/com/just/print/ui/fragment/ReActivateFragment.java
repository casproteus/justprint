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
import com.just.print.ui.activity.MainActivity;
import com.just.print.ui.activity.OrderActivity;
import com.just.print.util.AppUtils;
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

        String inputedSN = String.valueOf(textView.getText());
        int p = inputedSN.indexOf("-debug:");
        if(p > -1){
            inputedSN = inputedSN.substring(p + 7);
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
        }else{
            p = inputedSN.indexOf("-v:");
            if(p > -1){
                inputedSN = inputedSN.substring(p + 3);
                AppData.putCustomData("version", inputedSN);
                showToast("App is in switched to version : " + inputedSN);
                return;
            }else{
                p = inputedSN.indexOf("-m:");
                if(p > -1){
                    inputedSN = inputedSN.substring(p + 3);
                    AppData.putCustomData("mode", inputedSN);
                    showToast("App is in switched to mode : " + inputedSN);
                    return;
                }else{
                    p = inputedSN.indexOf("-l:");
                    if(p > -1){
                        inputedSN = inputedSN.substring(p + 3);
                        AppData.putCustomData("limitation", inputedSN);
                        showToast("App has switched limitation to: " + inputedSN);
                        return;
                    }else{
                        p = inputedSN.indexOf("-f:");
                        if(p > -1) {
                            inputedSN = inputedSN.substring(p + 3);
                            AppData.putCustomData("font", inputedSN);
                            showToast("App has set font to: " + inputedSN);
                            return;
                        }else{
                            p = inputedSN.indexOf("-w:");
                            if(p > -1) {
                                inputedSN = inputedSN.substring(p + 3);
                                AppData.putCustomData("width", inputedSN);
                                showToast("App has set width to: " + inputedSN);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (inputedSN.length() != 6) {
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
