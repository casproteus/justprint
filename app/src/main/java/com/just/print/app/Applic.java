package com.just.print.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.just.print.db.dao.DaoMaster;
import com.just.print.net.UDPService;
import com.just.print.util.L;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by wangx on 2016/10/28.
 */
public class Applic extends Application {
    volatile static Applic app;

    public static Applic getApp() {
        return app;
    }

    public UDPService mUDPService;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUDPService = ((UDPService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.i(TAG, "service disconnected!");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, UDPService.class));
        app = this;
        new XCrashHandler().init(this);// 异常处理
        bindService(new Intent(this, UDPService.class), mServiceConnection, BIND_AUTO_CREATE);
    }


    private DaoMaster mDaoMaster;

    public void initDaoMaster(String shopName) {
        if (mDaoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
                    getDBPath(shopName).getAbsolutePath(), null);
            mDaoMaster = new DaoMaster(helper.getWritableDatabase());
        }
    }

    public File getDBPath(String dbname) {
        return getDatabasePath("JustPrinter_" + dbname);
    }

    public DaoMaster newDaoMaster(String dbname) {

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
                getDBPath(dbname).getAbsolutePath(), null);
        DaoMaster daomaster = new DaoMaster(helper.getWritableDatabase());
        return daomaster;
    }

    public DaoMaster getDaoMaster() {
        if (mDaoMaster == null) {
            new NullPointerException("请初始化数据库");
//            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
//                    "JustPrinter", null);
//            mDaoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return mDaoMaster;
    }

}
