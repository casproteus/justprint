package com.just.print.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;

import com.just.print.Activate;
import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseActivity;
import com.just.print.ui.holder.ActivityViewHolder;
import com.just.print.util.L;
import com.just.print.util.ToastUtil;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;

import java.util.Arrays;
import java.util.Date;

public class MainActivity extends BaseActivity {

//    ServiceConnection serviceConnection;
//    UDPService udp;
    private static String isDebug = AppData.getCustomData("Debug");
    public static boolean debug = isDebug == null ? false : Boolean.valueOf(isDebug);

    //@note: there's a issue in this method!!!!!
    //@in some device, it always return a negative value and cause reactive dlg displayed.
    private long checkDaysleft() {
        Object limitationMode = AppData.getCustomData("limitation");        L.d("limitationMode:", limitationMode);

        if("none".equals(limitationMode)){
            return 3024000000l + 1;
        }
        long currentTime = new Date().getTime();                            L.d("currentTime:", currentTime);

        //time of last open, if existing number is not valid, then use current time as last open time.
        String lastsuccessStr = AppData.getCustomData("lastsuccessStr");    L.d("lastSuccessStr:",lastsuccessStr);
        long lastSuccess = 0l;
        try{
            lastSuccess= Long.valueOf(lastsuccessStr);
        }catch(Exception e){
            lastSuccess = currentTime;
        }

        long timepassed = currentTime - lastSuccess;                        L.d("timePassed:",timepassed);
        //time passed since last open.

        String timeLeftStr = AppData.getCustomData("number");               L.d("timeLeft(before deduct:", timeLeftStr);
        long timeLeft = 0;

        //if timeLeftStr is valid, then it has a chance to turn the timeLeft to be a number bigger than 0.
        if (timeLeftStr != null && timeLeftStr.length() > 0) {
            try {
                //the time left from last calculation, minus time passed. @note: we use abs, so is the time is negative, will still be minused!
                timeLeft = Long.valueOf(timeLeftStr) - Math.abs(timepassed);    L.d("timeLeft - timePassed:", timeLeft);

                //update the number and lastsuccess into local cache.
                AppData.putCustomData("lastsuccessStr", String.valueOf(currentTime));   L.d("update new lastSuccess string with:", currentTime);
                AppData.putCustomData("number", String.valueOf(timeLeft));              L.d("update new number with:", timeLeft);
            }catch(Exception e){
                L.e("MainActivity", "the left time number can not be pasered into a long", e);
            }
        }

        return timeLeft;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        new StupidReflect(this).init();
        try {
            debug = Boolean.valueOf(AppData.getCustomData("debug"));
        }catch(Exception e){
            L.i("the value of debug in customerr Data ", "can  not be parsered into a boolean!");
        }

        long timeLeft = checkDaysleft();
        if (timeLeft > 0) {
            if (timeLeft < 3024000000l) {
                ToastUtil.showToast("Application is about to expire! Please re-activate it!");
            }
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            ToastUtil.showToast("Application expired! Please re-activate it!");
            startActivity(new Intent(this, Activate.class));
        }
        finish();
        return;

        //@TODO: don't understand when should we use this part?
//        if(false) {
//            ListView listView = (ListView) findViewById(R.id.listView);
//            XAdapter2<ActivityInfo> adapter = new XAdapter2<ActivityInfo>(this, ActivityViewHolder.class);
//            try {
//                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
//                adapter.addAll(Arrays.asList(info.activities));
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            listView.setAdapter(adapter);
//        }
//        bindService(new Intent(this, UDPService.class), serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                udp = ((UDPService.MyBinder) service).getService();
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        }, BIND_AUTO_CREATE);
    }


}
