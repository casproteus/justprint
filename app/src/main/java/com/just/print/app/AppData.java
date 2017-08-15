package com.just.print.app;

import android.content.Context;

import com.just.print.util.SharedPreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by wangx on 2016/11/4.
 */
public class AppData {
    public static final String KEY_SHOP_XML = "KEY_SHOP_XML";
    public static final String KEY_SHOP_LIST = "KEY_SHOP_LIST";
    public static final String KEY_SHOP_ID = "KEY_SHOP_ID";
    private static final String KEY_PREFIX_SHOP_ID_ = "KEY_SHOP_ID_";
    private static final String KEY_SHOP_NAME = "shopName";

    public static SharedPreferencesHelper getShopData(Context context) {
        return SharedPreferencesHelper.getCache(context, KEY_SHOP_XML);
    }


    /**
     * 判断店铺是否存在
     *
     * @param context
     * @param shopName
     * @return true 存在,false 不存在
     **/
    public static boolean existShop(Context context, String shopName) {
        String shoplist = getShopData(context).getString(KEY_SHOP_LIST, "");
        return shoplist.contains(shopName);
    }

    /**
     * @param context
     * @param shopName
     * @return id
     **/
    public static void createShopDB(Context context, String shopName) {
        if (!existShop(context, shopName)) {
            String shoplist = getShopData(context).getString(KEY_SHOP_LIST, "");
            getShopData(context).putString(KEY_SHOP_LIST, shoplist + "," + shopName);
        }
        Applic.app.initDaoMaster(shopName);
    }

    public static void saveShopName(Context context, String shopName) {
        getShopData(context).putString(KEY_SHOP_NAME, shopName);
    }

    public static String getShopName(){
        return getShopName(Applic.app.getApplicationContext());
    }
    public static String getShopName(Context context) {
        return getShopData(context).getString(KEY_SHOP_NAME, "");
    }

    public static void saveUserName(Context context, String userName) {
        getShopData(context).putString("userName", userName);
    }

    public static String getUserName() {
        return getShopData(Applic.app.getApplicationContext()).getString("userName", "");
    }
    public static String getUserName(Context context) {
        return getShopData(context).getString("userName", "");
    }

    public static String getLicense() {
        return getLicense(Applic.app.getApplicationContext());
    }
    public static String getLicense(Context context) {
        return getShopData(context).getString("license", "");
    }

    public static void setLicense(Context context, String license) {
        getShopData(context).putString("license", license);
    }

    public static void putCustomData(Context baseContext, String key, String value) {

        getShopData(baseContext).putString("custom_" + key, value);
    }

    public static String getLastSyncDate(){
        return getShopData(Applic.app.getApplicationContext()).getString("LastSyncDate","");
    }
    public static void updataeLastSyncDate(String lastUpdateTime) {
        getShopData(Applic.app.getApplicationContext()).putString("LastSyncDate",
                lastUpdateTime != null && lastUpdateTime.length() > 1 ? lastUpdateTime : String.valueOf(new Date().getTime()));
    }

    public static String getCustomData(Context context, String key) {
        return getShopData(context).getString("custom_" + key, "");
    }
}
