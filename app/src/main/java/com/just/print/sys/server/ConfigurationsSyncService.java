package com.just.print.sys.server;

import android.util.Log;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.util.StringUtils;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据同步服务
 * Created by wangx on 2016/10/31.
 */
public class ConfigurationsSyncService extends Thread{

    public String configurationStr;
    public String serverip;
    public boolean isDownload;
    @Override
    public void run() {     //must use bk, because if use CustomerSelection.getInstance().getSelectedDishes(), it might be cleaned before thread started.
        if(configurationStr != null) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = AppData.prepareConnection("http://" + serverip + "/justprint/" + (isDownload ? "downloadConf" : "uploadConf"));
                JSONObject json = new JSONObject();//创建json对象

                String license = AppData.getLicense();
                String storName = AppData.getShopName();
                json.put("filepath", URLEncoder.encode(license + storName, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("billIndex", URLEncoder.encode(configurationStr, "UTF-8"));//把数据put进json对象中

                AppData.writeOut(urlConnection, json.toString());//把JSON对象按JSON的编码格式转换为字符串

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
                    String rjson = AppData.readBackFromConnection(urlConnection);

                    Log.d("zxy", "rjson=" + rjson);//rjson={"json":true}
                } else {
                    Log.e("L", "response code is:" + urlConnection.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("L", "Exception happened when sending dishes to server: " + serverip);//rjson={"json":true}
            } finally {
                configurationStr = null;
                if (urlConnection != null) {
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                }
            }
        }
    }
}
