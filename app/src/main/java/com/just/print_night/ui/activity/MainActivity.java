package com.just.print_night.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.just.print_night.Activate;
import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.BaseActivity;
import com.just.print_night.util.L;
import com.just.print_night.util.ToastUtil;
import com.stupid.method.reflect.StupidReflect;

import java.util.Date;

public class MainActivity extends BaseActivity {

    //    ServiceConnection serviceConnection;
//    UDPService udp;
    private static String isDebug = AppData.getCustomData("Debug");
    public static boolean debug = isDebug == null ? false : Boolean.valueOf(isDebug);

    //@note: there's a issue in this method!!!!!
    //@in some device, it always return a negative value and cause reactive dlg displayed.
    private long checkDaysleft() {
        //none limitation check
        if("none".equals(AppData.getCustomData("limitation"))){             L.d("limitationMode", "none");
            return 3024000000l + 1;
        }

        //time of last open, @note:if existing last open time is not valid, then use lastSuccess will not be set.
        long timepassed = 0l;
        String lastsuccessStr = AppData.getCustomData("lastsuccessStr");    L.d("lastSuccessStr:",lastsuccessStr);
        try{
            Long lastSuccess= Long.valueOf(lastsuccessStr);
            timepassed = new Date().getTime() - lastSuccess;L.d("timePassed:",timepassed);        //time passed since last open.
        }catch(Exception e){
            L.e("MainActivity", "the lastsuccessStr is not valid long", e);
        }

        //if timeLeftStr is valid, then it has a chance to turn the timeLeft to be a number bigger than 0.
        long timeLeft = 0;
        String number = AppData.getCustomData("number");               L.d("timeLeft(before deduct:", number);
        try {
            //the time left from last calculation, minus time passed. @note: we use abs, so is the time is negative, will still be minused!
            timeLeft = Long.valueOf(number) - Math.abs(timepassed);    L.d("timeLeft - timePassed:", timeLeft);

            //update the number and lastsuccess into local cache.
            AppData.putCustomData("lastsuccessStr", String.valueOf(new Date().getTime()));
            AppData.putCustomData("number", String.valueOf(timeLeft));              L.d("update new number with:", timeLeft);
        }catch(Exception e){
            L.e("MainActivity", "the left time number can not be pasered into a long", e);
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
            if (timeLeft < 3024000000l) {   //3024000000L == 35days
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
