package com.just.print.sys.server;

import android.util.Log;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.sys.model.SelectionDetail;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据同步服务
 * Created by wangx on 2016/10/31.
 */
public class DataSyncService extends Thread{

    public ArrayList<SelectionDetail> lastSelection;
    protected String serverip;

    @Override
    public void run() {     //must use bk, because if use CustomerSelection.getInstance().getSelectedDishes(), it might be cleaned before thread started.
        if(lastSelection != null && lastSelection.size() > 0) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = AppData.prepareConnection("http://" + serverip + "/security/newOrders");
                JSONObject json = new JSONObject();//创建json对象
                json.put("table", URLEncoder.encode(AppData.getUserName(), "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("billIndex", URLEncoder.encode("this is a test", "UTF-8"));//把数据put进json对象中
                StringBuilder orderContent = new StringBuilder();
                for (SelectionDetail selectionDetail : lastSelection) {
                    orderContent.append("DishStart:");
                    Menu menu = selectionDetail.getDish();
                    Category categoryStr = Applic.app.getDaoMaster().newSession().getCategoryDao().load(menu.getCid());
                    orderContent.append(URLEncoder.encode(categoryStr.getCname(), "UTF-8")).append("\n");
                    orderContent.append(URLEncoder.encode(menu.getMname(), "UTF-8")).append("\n");

                    orderContent.append(selectionDetail.getDish().getPrice()).append("\n");
                    orderContent.append(selectionDetail.getDishNum()).append("\n");

                    List<Mark> marks = selectionDetail.getMarkList();
                    orderContent.append("MarkStart:");
                    for (Mark mark : marks) {
                        orderContent.append(URLEncoder.encode(mark.getName(), "UTF-8")).append("\n");
                        orderContent.append(mark.getQt()).append("\n");
                        orderContent.append(mark.getState()).append("\n");
                        orderContent.append(mark.getVersion()).append("\n");
                    }

                    json.put("orderContent", orderContent.toString());
                }

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
                if (urlConnection != null) {
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                }
            }
        }
    }
}
