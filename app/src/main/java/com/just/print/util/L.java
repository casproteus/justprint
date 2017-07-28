package com.just.print.util;

import android.util.Log;

import com.just.print.app.Applic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Simon&Nicholas on 7/28/2017.
 */

public class L {
    public static void d(String tag, String msg){
        sendToServer(tag, msg);
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e){
        sendToServer(tag, msg);
        Log.i(tag, msg, e);
    }

    public static void i(String tag, String msg){
        sendToServer(tag, msg);
        Log.i(tag, msg);
    }
    public static void w(String tag, String msg, Throwable e){
        sendToServer(tag, msg);
        Log.d(tag, msg, e);
    }

    public static void sendToServer(String tag, String msg){
        boolean debug = false;
        if(debug && AppUtils.hasInternet(Applic.getApp().getApplicationContext())){
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";
            try {
                URL realUrl = new URL("http://localhost:/");
                //打开和URL之间的连接
                URLConnection conn = realUrl.openConnection();
                //设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                //发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                //发送请求参数
                out.print("msg");
                //flush 输出流的缓冲
                out.flush();
                //定义BufferedReader输入流来读取URL的响应
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += "n" + line;
                }
            } catch (Exception e) {
                System.out.println("发送POST 请求出现异常！" + e);
                e.printStackTrace();
            }
            //使用finally块来关闭输出流、输入流
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
