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
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;

import java.util.Arrays;

public class MainActivity extends BaseActivity {

//    ServiceConnection serviceConnection;
//    UDPService udp;
    boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        new StupidReflect(this).init();

        if (!debug) {
            Activate.currentSN = AppData.getLicense(this);
            if (AppData.getLicense(this).length() > 0)
                startActivity(new Intent(this, LoginActivity.class));
            else
                startActivity(new Intent(this, Activate.class));
            finish();
            return;
        }

        if(debug) {
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
