package com.just.print.app;

import android.content.Context;

import com.just.print.util.SharedPreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wangx on 2016/11/4.
 */
public class AppData {
    public static final String KEY_SHOP_XML = "KEY_SHOP_XML";
    public static final String KEY_SHOP_LIST = "KEY_SHOP_LIST";
    public static final String KEY_SHOP_ID = "KEY_SHOP_ID";
    private static final String KEY_PREFIX_SHOP_ID_ = "KEY_SHOP_ID_";

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
        Applic.getApp().initDaoMaster(shopName);
    }

    public static void saveShopName(Context context, String shopName) {
        getShopData(context).putString("shopName", shopName);
    }

    public static String getShopName(Context context) {
        return getShopData(context).getString("shopName", "");
    }

    public static void saveUserName(Context context, String userName) {
        getShopData(context).putString("userName", userName);
    }

    public static String getUserName(Context context) {
        return getShopData(context).getString("userName", "");
    }

    public static String getLicense(Context context) {
        return getShopData(context).getString("license", "");
    }

    public static void setLicense(Context context, String license) {
        getShopData(context).putString("license", license);
    }

    public static void writeDBByte(Context context, byte[] datas, String dbname) {

        File file = context.getDatabasePath("JustPrinter_" + dbname);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(datas);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void putCustomData(Context baseContext, String key, String value) {

        getShopData(baseContext).putString("custom_" + key, value);
    }

    public static String getCustomData(Context context, String key) {
        return getShopData(context).getString("custom_" + key, "");
    }
}
