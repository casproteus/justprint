package com.just.print.util;

import android.util.Log;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.ui.activity.MainActivity;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * Created by Simon&Nicholas on 7/28/2017.
 */

public class L extends Thread{
    private  String tag;
    private String msg;

    public L(String tag, String msg){
        this.tag = tag;
        this.msg = msg;
    }
    public static void d(String tag, String msg){
        sendToServer(AppData.getShopName()+"_"+AppData.getUserName(), tag + ":" + msg);
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e){
        sendToServer(AppData.getShopName()+"_"+AppData.getUserName(), tag + ":" + msg);
        Log.i(tag, msg, e);
    }

    public static void i(String tag, String msg){
        sendToServer(AppData.getShopName()+"_"+AppData.getUserName(), tag + ":" + msg);
        Log.i(tag, msg);
    }
    public static void w(String tag, String msg, Throwable e){
        sendToServer(AppData.getShopName()+"_"+AppData.getUserName(), tag + ":" + msg);
        Log.d(tag, msg, e);
    }

    public static void sendToServer(String tag, String msg){
        new L(tag, msg).start();
    }

    @Override
    public void run() {
        super.run();
        if(MainActivity.debug && AppUtils.hasInternet(Applic.app.getApplicationContext())){

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = AppData.prepareConnection("http://teamup.sharethegoodones.com/useraccounts/loglog");

                JSONObject json = new JSONObject();//创建json对象
                json.put("tag", URLEncoder.encode(AppData.getUserName(), "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("msg", URLEncoder.encode(tag + "-" + msg, "UTF-8"));//把数据put进json对象中
                String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串

                AppData.writeOut(urlConnection, jsonstr);

                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功

                    String rjson = AppData.readBackFromConnection(urlConnection);

                    Log.d("zxy", "rjson="+rjson);//rjson={"json":true}
                }else{
                }
            } catch (Exception e) {
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
        }
    }
}
