package com.just.print_night.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.just.print_night.db.dao.DaoMaster;
import com.just.print_night.db.dao.MarkDao;
import com.just.print_night.net.UDPService;
import com.just.print_night.util.L;

import java.io.File;

import static android.content.ContentValues.TAG;

public class Applic extends Application {

    public volatile static Applic app;

    public UDPService mUDPService;

    private DaoMaster daoMaster;

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
        new XCrashHandler().init(this);// for unhanddle.
        bindService(new Intent(this, UDPService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    public void initDaoMaster(String shopName) {
        if (daoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
                    getDBPath(shopName).getAbsolutePath(), null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
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
        if (daoMaster == null) {
            L.e("Applic","non-none daoMaster expected! db not inited!", null);
            new NullPointerException("Please initialize database!");
        }
        return daoMaster;
    }

    public static MarkDao getMarkDao(){
        return app.getDaoMaster().newSession().getMarkDao();
    }
}
