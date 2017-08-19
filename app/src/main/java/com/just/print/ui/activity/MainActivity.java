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

    private long checkDaysleft() {

        long currentTime = new Date().getTime();
        //time of last open
        String lastsuccessStr = AppData.getCustomData("lastsuccessStr");
        long lastSuccess = 0l;
        try{
            lastSuccess= Long.valueOf(lastsuccessStr);
        }catch(Exception e){
            lastSuccess = currentTime;
        }

        //time passed since last open.
        long timepassed = currentTime - lastSuccess;

        String timeLeftStr = AppData.getCustomData("number");
        long timeLeft = 0;

        if (timeLeftStr != null && timeLeftStr.length() > 0) {
            try {
                timeLeft = Long.valueOf(timeLeftStr) - Math.abs(timepassed);

                if (timeLeft > 0 && timeLeft < 34560000000l) {
                    AppData.putCustomData("lastsuccessStr", String.valueOf(currentTime));
                    AppData.putCustomData("number", String.valueOf(timeLeft));
                } else {
                    AppData.putCustomData("lastsuccessStr", String.valueOf(currentTime));
                }
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

        if (!debug) {
            long timeLeft = checkDaysleft();
            if (timeLeft > 0) {
                if(timeLeft < 3024000000l){
                    ToastUtil.showToast("Application expired! Please re-activate it!");
                }
                startActivity(new Intent(this, LoginActivity.class));
            } else
                startActivity(new Intent(this, Activate.class));
            finish();
            return;
        } else {
            ListView listView = (ListView) findViewById(R.id.listView);
            XAdapter2<ActivityInfo> adapter = new XAdapter2<ActivityInfo>(this, ActivityViewHolder.class);
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                adapter.addAll(Arrays.asList(info.activities));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            listView.setAdapter(adapter);
        }
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
