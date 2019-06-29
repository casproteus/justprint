package com.just.print_night.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.just.print_night.Activate;
import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.BaseActivity;
import com.just.print_night.ui.fragment.ConfigPrinterFragment;
import com.just.print_night.ui.holder.OrderMarksSelectionViewHolder;
import com.just.print_night.util.L;
import com.just.print_night.util.StringUtils;
import com.just.print_night.util.ToastUtil;
import com.stupid.method.reflect.StupidReflect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity {

    //    ServiceConnection serviceConnection;
//    UDPService udp;
    private static String isDebug = AppData.getCustomData("debug");
    public static boolean debug = isDebug == null ? false : Boolean.valueOf(isDebug);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        new StupidReflect(this).init();

        //this block should be useless, because dubug is already set value.
        // or maybe onCreate method could be called more than one time? so every time swith back to this page,
        // the oncreate will be called again? so we can update the debug setting without restart app?
        try {
            debug = Boolean.valueOf(AppData.getCustomData("debug"));
        }catch(Exception e){
            L.i("the value of debug in customerr Data ", "can  not be parsered into a boolean!");
        }

        //working time check
        if(isOutOfWorkingTime()){
            ToastUtil.showToast("Invalidate operation detected, please contact info@ShareTheGoodOnes.com for technique support!");
            return;
        }

        //software life left time check
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
        String lastTimeLeft = AppData.getCustomData("number");               L.d("timeLeft(before deduct:", lastTimeLeft);
        if(StringUtils.isBlank(lastTimeLeft)){
            lastTimeLeft = "1";
        }
        try {
            //the time left from last calculation, minus time passed. @note: we use abs, so is the time is negative, will still be minused!
            timeLeft = Long.valueOf(lastTimeLeft) - Math.abs(timepassed);    L.d("timeLeft - timePassed:", timeLeft);

            //update the number and lastsuccess into local cache.
            AppData.putCustomData("lastsuccessStr", String.valueOf(new Date().getTime()));
            AppData.putCustomData("number", String.valueOf(timeLeft));              L.d("update new number with:", timeLeft);
        }catch(Exception e){
            L.e("MainActivity", "the left time number can not be pasered into a long", e);
        }

        return timeLeft;
    }

    private boolean isOutOfWorkingTime(){

        String startTime = AppData.getCustomData("startTime");
        String endTime = AppData.getCustomData("endTime");
        String curTime = "";

        DateFormat df = new SimpleDateFormat("HHmm");
        try {
            Date d = new Date();
            curTime = df.format(d);
        }catch(Exception e){
            ToastUtil.showToast("Error detected with system time, please contact info@ShareTheGoodOnes.com for technique support.");
        }

        if(!StringUtils.isBlank(startTime)){
            if(curTime.compareTo(startTime) < 0) {
                return true;
            }
        }

        if(!StringUtils.isBlank(endTime)){
            if(curTime.compareTo(endTime) > 0) {
                return true;
            }
        }

        return false;
    }

}
