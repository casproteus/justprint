package com.just.print.util;

import android.util.Log;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.ui.activity.MainActivity;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Vector;

/**
 * Created by Simon&Nicholas on 7/28/2017.
 */

public class L extends Thread{
    private static int index = 0;

    private static L instance = null;
    private static Vector<String> msgs = new Vector<String>();

    public static void d(String tag, Object msg){
        sendToServer(AppData.getShopName()+"_"+AppData.getUserName(), tag + ":" + String.valueOf(msg));
        Log.d(tag, String.valueOf(msg));
    }

    public static void e(String tag, String msg, Throwable e){
        MainActivity.debug = true;
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
        if(MainActivity.debug && AppUtils.hasInternet(Applic.app.getApplicationContext())) {
            if(instance == null) {
                instance = new L();
                instance.start();
            }
            msgs.add(index++ + "_" + msg);
        }
    }

    @Override
    public void run() {
        super.run();
        HttpURLConnection urlConnection = null;
        while(true){
            if(msgs.size() > 0) {
                String msg = msgs.get(0);
                msgs.remove(0);
                try {
                    urlConnection = AppData.prepareConnection("http://team.sharethegoodones.com/useraccounts/loglog");
                    //urlConnection = AppData.prepareConnection("http://192.168.1.234/bigbang/useraccounts/loglog");

                    JSONObject json = new JSONObject();//创建json对象
                    json.put("tag", URLEncoder.encode(AppData.getUserName(), "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                    json.put("msg", URLEncoder.encode(msg, "UTF-8"));//把数据put进json对象中
                    String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串

                    AppData.writeOut(urlConnection, jsonstr);

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功

                        String rjson = AppData.readBackFromConnection(urlConnection);

                        Log.d("zxy", "rjson=" + rjson);//rjson={"json":true}
                    } else {
                        Log.d("L", "response code is:" + urlConnection.getResponseCode());
                    }
                } catch (Exception e) {
                    Log.d("L", "Exception happened when sending log to server: " + urlConnection.getURL());//rjson={"json":true}
                } finally {
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                }
            }
            AppUtils.sleep(1000);
        }
    }
}
