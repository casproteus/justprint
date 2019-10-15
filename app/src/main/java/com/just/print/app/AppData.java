package com.just.print.app;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.just.print.db.bean.Printer;
import com.just.print.ui.activity.MainActivity;
import com.just.print.ui.fragment.LoginFragment;
import com.just.print.util.AppUtils;
import com.just.print.util.L;
import com.just.print.util.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import static com.just.print.util.ToastUtil.showToast;

public class AppData extends Thread{
    private static String SERVER_URL = "http://www.sharethegoodones.com";

    public static final String KEY_SHOP_XML = "KEY_SHOP_XML";
    public static final String KEY_SHOP_LIST = "KEY_SHOP_LIST";
    public static final String KEY_SHOP_ID = "KEY_SHOP_ID";
    private static final String KEY_PREFIX_SHOP_ID_ = "KEY_SHOP_ID_";
    private static final String KEY_SHOP_NAME = "shopName";

    //all keys
    //no sync
    public static final String debug = "debug";
    public static final String limitation = "limitation";
    public static final String lastsuccessStr = "lastsuccessStr";
    public static final String number = "number";
    public static final String startTime = "startTime";
    public static final String endTime = "endTime";
    public static final String reportStartDate = "reportStartDate";
    public static final String lastsuccess = "lastsuccess";
    public static final String reportIdx = "reportIdx";
    public static final String AllowDownloadFromeOthers = "AllowDownloadFromeOthers";
    public static final String kitchenBillIdx = "kitchenBillIdx";
    public static final String font = "font";
    public static final String mode = "mode";
    public static final String code = "code";
    public static final String BeiYangPrinter = "BeiYangPrinter";
    public static final String autoSearchBeiYang = "autoSearchBeiYang";
    public static final String ContentToSend = "ContentToSend";

    //to sync
    public static final String server_url = "server_url";
    public static final String appmode = "appmode";
    public static final String ShowMarkPirce = "ShowMarkPirce";
    public static final String userPassword = "userPassword";
    public static final String adminPassword = "adminPassword";
    public static final String custChars = "custChars";
    public static final String column = "column";
    public static final String serverip = "serverip";
    public static final String reportPrinter = "reportPrinter";
    public static final String waitTime = "waitTime";
    public static final String conbineMarkPrice = "conbineMarkPrice";
    public static final String reportFont = "reportFont";
    public static final String reportWidth = "reportWidth";
    public static final String sep_str1 = "sep_str1";
    public static final String sep_str2 = "sep_str2";
    public static final String menuNameLength = "menuNameLength";
    public static final String width = "width";
    public static final String kitchentitle = "kitchentitle";
    public static final String format_style = "format_style";
    public static final String title_position = "title_position";
    public static final String priceonkitchenbill = "priceonkitchenbill";
    public static final String KEY_CUST_LAST_CHAR = "KEY_CUST_LAST_CHAR";
    public static final String hideCancelItem = "hidecancelitem";
    public static final String sendReport = "sendreport";
    public static final String sendOnlyWhenReset = "sendonlywhenreset";
    public static final String HideKitchenBillId = "hikiid";
    public static final String HideKitchenBillName = "hikina";
    public static final String ColorOnSelect = "cos";

    public static String[] keysToSync = new String[]{server_url, appmode, ShowMarkPirce, userPassword, adminPassword , custChars,
            column, serverip, reportPrinter, waitTime, conbineMarkPrice,
            reportFont, reportWidth, sep_str1, sep_str2, menuNameLength,
            width, kitchentitle, format_style, title_position, priceonkitchenbill,
            KEY_CUST_LAST_CHAR, hideCancelItem,sendReport,sendOnlyWhenReset, HideKitchenBillId,
            HideKitchenBillId, ColorOnSelect};
    public static String curBillIdx;

    private static SharedPreferencesHelper getShopData(Context context) {
        return SharedPreferencesHelper.getCache(context, KEY_SHOP_XML);
    }

    /**
     * 判断店铺是否存在
     *
     * @param context
     * @param shopName
     * @return true 存在,false 不存在
     **/
    private static boolean existShop(Context context, String shopName) {
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

    public static void createPrinter(
            String ip,
            String name,
            int fullPrintMode, //是否全单打印,1 全单打印,0单独打印
            int checkid,
            String note) {
        Printer printer = new Printer();
        printer.setFirstPrint(fullPrintMode);
        printer.setPname(name);
        printer.setIp(ip);
        printer.setType(checkid);// 1: 顺序打印,0 :类别打印
        printer.setNote(note);
        //        if (checkBox.isChecked()) {
        //            //DaoExpand.updateAllPrintTo0(getDaoMaster().newSession().getPrinterDao());
        //            printer.setFirstPrint(1);
        //        }
        printer.setState(com.just.print.db.expand.State.def);
        Applic.app.getDaoMaster().newSession().getPrinterDao().insert(printer);
        printer.updateAndUpgrade();
    }

    public static void saveCustomizedLastCharOnPanel(Context context, String character) {
        getShopData(context).putString(KEY_CUST_LAST_CHAR, character);
    }

    public static String getSERVER_URL(){
        String serverURL = AppData.getCustomData(AppData.server_url);
        if(serverURL == null || serverURL.length() < 10) {
            serverURL = AppData.SERVER_URL;
        }
        if(!serverURL.startsWith("http://")){
            serverURL = "http://" + serverURL;
        }
        return serverURL;
    }

    public static String getCustomizedLastCharOnPanel(Context context){
        return getShopData(context).getString(KEY_CUST_LAST_CHAR, "");
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

    public static void setLicense(String inputedSN, TextView textView) {
        if(inputedSN.startsWith("-") && (inputedSN.indexOf(":") > 1 || inputedSN.indexOf("：") > 1)){
            setCustomValue(inputedSN, textView);
            return;
        }else if(inputedSN.startsWith("-") && (inputedSN.endsWith("?") || inputedSN.endsWith("？"))) {
            displayCustomValue(inputedSN);
            return;
        }else if (inputedSN.length() != 6) {
            showToast("Please input correct license code");
            return;
        }

        getShopData(Applic.app.getApplicationContext()).putString("license", inputedSN);
        startActivate();
    }

    public static void putCustomData(String key, String value) {
        getShopData(Applic.app.getApplicationContext()).putString("custom_" + key.toLowerCase(), value);
    }
    public static String getCustomData(String key) {
        return getShopData(Applic.app.getApplicationContext()).getString("custom_" + key.toLowerCase(), "");
    }

    public static String getLastModifyTime(){
        return getShopData(Applic.app.getApplicationContext()).getString("LastSyncDate","");
    }

    public static void updataeLastModifyTime(String lastUpdateTime) {
        getShopData(Applic.app.getApplicationContext()).putString("LastSyncDate",
                lastUpdateTime != null && lastUpdateTime.length() > 1 ? lastUpdateTime : String.valueOf(new Date().getTime()));
    }

    public static HttpURLConnection prepareConnection(String uri) throws Exception {
        HttpURLConnection urlConnection;
        URL url = new URL(uri);
        urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
        urlConnection.setConnectTimeout(30000);//连接的超时时间
        urlConnection.setUseCaches(false);//不使用缓存
        //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
        urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
        urlConnection.setReadTimeout(30000);//响应的超时时间
        urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
        urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
        urlConnection.setRequestMethod("POST");//设置请求的方式
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
        urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接
        return urlConnection;
    }

    public static void writeOut(HttpURLConnection urlConnection, String jsonstr) throws Exception{
        //-------------使用字节流发送数据--------------
        //OutputStream out = urlConnection.getOutputStream();
        //BufferedOutputStream bos = new BufferedOutputStream(out);//缓冲字节流包装字节流
        //byte[] bytes = jsonstr.getBytes("UTF-8");//把字符串转化为字节数组
        //bos.write(bytes);//把这个字节数组的数据写入缓冲区中
        //bos.flush();//刷新缓冲区，发送数据
        //out.close();
        //bos.close();
        //------------字符流写入数据------------
        OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
        bw.write(jsonstr);//把json字符串写入缓冲区中
        bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
        out.close();
        bw.close();//使用完关闭
    }

    //When there are more mode2 than mode 1, will open the commented line.
    public static boolean isMode2() {
//        return getCustomData(AppData.appmode) == null || "".equals(getCustomData(AppData.appmode)) || "2".equals(getCustomData(AppData.appmode));
        return "2".equals(getCustomData(AppData.appmode));
    }

    public static String schema;

    public static void startActivate() {
        AppData.schema = AppData.getSERVER_URL() + "/activeJustPrintAccount";
        String accountInfo = AppData.getJsonObjectForActivate();
        if(accountInfo != null){
            AppData.putCustomData(AppData.ContentToSend, accountInfo);//把JSON对象按JSON的编码格式转换为字符串
            new AppData().start();
        }
    }

    private static String getJsonObjectForActivate() {
        StringBuilder content = new StringBuilder(AppData.getLicense());
        content.append(",");
        content.append(AppData.getShopName());
        content.append("-");
        content.append(AppData.getUserName());//如果有需要对特殊和不可见字符进行编码，使用URLEncoder.encode

        JSONObject json = new JSONObject();//创建json对象
        try {
            json.put("username", content.toString());//使用URLEncoder.encode对特殊和不可见字符进行编码
        }catch(Exception e){
            showToast("Please provide valid shop name, user name and license number!");
            L.e("TAG", "USERLOGIN_FAILED", e);
            return null;
        }
        return json.toString();
    }

    public static boolean notifyCheck(int idx, String reportContent, boolean isReset) {
        String email = AppData.getCustomData(AppData.sendReport);
        if(email != null && email.indexOf("@") > 0) {
            AppData.schema = AppData.getSERVER_URL() + "/sendReport";
            //if there's already a content, means it's email of last time. then send it out first.
            String unSendcontent = AppData.getCustomData(AppData.ContentToSend);
            if(unSendcontent != null && unSendcontent.length() > 0){
                showToast("Found uncompleted task!");
                new AppData().start();
                return false;
            }
            boolean  sendOnlyWhenResetFlag = "true".equals(AppData.getCustomData(AppData.sendOnlyWhenReset));
            if(sendOnlyWhenResetFlag && !isReset){
                return true;
            }
            JSONObject json = new JSONObject();//创建json对象
            try {
                String mobileMark = isReset && !sendOnlyWhenResetFlag ? "(RESET)" + LoginFragment.getUserName() : LoginFragment.getUserName();
                json.put("idx", mobileMark + "-" + idx);
                json.put("email", email);//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", URLEncoder.encode(reportContent, "UTF-8"));//把数据put进json对象中
            } catch (Exception e) {
                L.e("DatabaseUtil", "Exception when encoding content into json: email:" + email + " reportContent:" + reportContent, e);
            }
            AppData.putCustomData(AppData.ContentToSend,json.toString());
            new AppData().start();
            return true;
        }
        return true;
    }

    public static int getThemColor() {
        String color = AppData.getCustomData(AppData.ColorOnSelect);
        if(color == null || color.length() != 6){
            color = "0091D5";
        }
        return Color.parseColor("#" + color);
    }

    @Override
    public void run() {
        super.run();
        String contentToSend = AppData.getCustomData(AppData.ContentToSend);
        if(contentToSend != null && contentToSend.length() > 0 && AppUtils.hasInternet(Applic.app.getApplicationContext())){
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = prepareConnection(schema);
                writeOut(urlConnection, contentToSend);

                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功

                    String responseString = readBackFromConnection(urlConnection);
                    AppData.putCustomData(AppData.ContentToSend, null);
                    long time = 0;
                    try{
                        time = Long.valueOf(responseString);
                        if(time > 0){//if success
                            AppData.putCustomData(AppData.number,responseString);
                            AppData.putCustomData(AppData.lastsuccess, String.valueOf(new Date().getTime()));

                            showToast("Application is activated successfully!");
                        }else{
                            showToast("No time left on server: "+ time);
                        }
                    }catch (Exception e){
                        //so the return back is configurations instead of a number(left time)
                        absortTheConfiguration(responseString);
                    }
                }else{
                    showToast("Please provide valid shop name, user name and license number!");
                }
            } catch (Exception e) {
                L.e("TAG", "USERLOGIN_FAILED", e);
                showToast("USERLOGIN_FAILED");
            }finally{
                if(urlConnection != null)
                    urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
        }
    }

    private void absortTheConfiguration(String responseString) {
        String[] strs = responseString.split(",");
        for (String entry : strs) {
            int i = entry.indexOf(":");
            if(i > 0){
                AppData.putCustomData(entry.substring(0, i), entry.substring(i + 1));
            }
        }

    }

    @NonNull
    public static String readBackFromConnection(HttpURLConnection urlConnection) throws IOException, JSONException {
        //------------字节流读取服务端返回的数据------------
        //InputStream in = urlConnection.getInputStream();//用输入流接收服务端返回的回应数据
        //BufferedInputStream bis = new BufferedInputStream(in);//高效缓冲流包装它，这里用的是字节流来读取数据的，当然也可以用字符流
        //byte[] b = new byte[1024];
        //int len = -1;
        //StringBuffer buffer = new StringBuffer();//用来接收数据的StringBuffer对象
        //while((len=bis.read(b))!=-1){
        //buffer.append(new String(b, 0, len));//把读取到的字节数组转化为字符串
        //}
        //in.close();
        //bis.close();
        //Log.d("zxy", buffer.toString());//{"json":true}
        //JSONObject rjson = new JSONObject(buffer.toString());//把返回来的json编码格式的字符串数据转化成json对象
        //------------字符流读取服务端返回的数据------------
        InputStream in = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String str = null;
        StringBuffer buffer = new StringBuffer();
        while((str = br.readLine())!=null){//BufferedReader特有功能，一次读取一行数据
            buffer.append(str);
        }
        in.close();
        br.close();
        return buffer.toString();
    }

    private static void displayCustomValue(String inputedSN) {
        inputedSN = inputedSN.substring(1, inputedSN.length() - 1);
        String answer = AppData.getCustomData(inputedSN);
        if(answer == null || answer.length() == 0) {
            answer = SharedPreferencesHelper.getCache(Applic.app.getApplicationContext(), inputedSN).getString(inputedSN);
        }
        showToast(inputedSN + " = " + answer);
    }

    private static void setCustomValue(String inputedSN, TextView textView) {
        int p = inputedSN.indexOf(":");
        if(p == -1){
            p = inputedSN.indexOf("：");
        }
        String SettingType = inputedSN.substring(1,p);
        inputedSN = inputedSN.substring(p + 1);
        switch (SettingType.toLowerCase()) {
            case "ap":   // means theres parameters
                AppData.putCustomData(AppData.adminPassword, inputedSN);
                showToast("App has set adminPassword to: " + inputedSN);
                return;
            case "am":   // means theres parameters
                AppData.putCustomData(AppData.appmode, inputedSN);
                showToast("App has set appmode to: " + inputedSN);
                return;
            case "c":
                AppData.putCustomData(AppData.code, inputedSN);
                showToast("App has set code to: " + inputedSN);
                return;
            case "cos":
                AppData.putCustomData(AppData.ColorOnSelect, inputedSN);
                showToast("App has set ColorOnSelect to: " + inputedSN);
                return;
            case "cn":
                AppData.putCustomData(AppData.column, inputedSN);
                showToast("App has set column to: " + inputedSN);
                return;
            case "cmp":
                AppData.putCustomData(AppData.conbineMarkPrice, inputedSN);
                showToast("App has set conbineMarkPrice to: " + inputedSN);
                return;
            case AppData.debug:
                MainActivity.debug = Boolean.valueOf(inputedSN);
                AppData.putCustomData(AppData.debug, String.valueOf(MainActivity.debug));
                if(MainActivity.debug) {
                    showToast("App is in debug mode, when bug appears again, please write down the system time and report.");

                    StringBuilder sb = new StringBuilder();
                    sb.append("adminPassword:").append(AppData.getCustomData(AppData.adminPassword)).append("\n");
                    sb.append("lastsuccess:").append(AppData.getCustomData(AppData.lastsuccess)).append("\n");
                    sb.append("limitation:").append(AppData.getCustomData(AppData.limitation)).append("\n");
                    sb.append("mode:").append(AppData.getCustomData(AppData.mode)).append("\n");
                    sb.append("number:").append(AppData.getCustomData(AppData.number)).append("\n");
                    sb.append("reportStartDate:").append(AppData.getCustomData(AppData.reportStartDate)).append("\n");
                    sb.append("userPassword:").append(AppData.getCustomData(AppData.userPassword)).append("\n");
                    textView.setText(sb.toString());
                }else{
                    showToast("debug mode turned off!");
                }
                return;
            case "endt":
                AppData.putCustomData(AppData.endTime, inputedSN);
                showToast("App has set endTime to: " + inputedSN);
                return;
            case "f":
                AppData.putCustomData(AppData.font, inputedSN);
                showToast("App has set font to: " + inputedSN);
                return;
            case "fsty":
                AppData.putCustomData(AppData.format_style, inputedSN);
                showToast("App has set format_style to: " + inputedSN);
                return;
            case "hci":
                AppData.putCustomData(AppData.hideCancelItem, inputedSN);
                showToast("App has set hideCancelItem to: " + inputedSN);
                return;
            case "hiid":
                AppData.putCustomData(AppData.HideKitchenBillId, inputedSN);
                showToast("App has set HideKitchenBillId to: " + inputedSN);
                return;
            case "hina":
                AppData.putCustomData(AppData.HideKitchenBillName, inputedSN);
                showToast("App has set HideKitchenBillName to: " + inputedSN);
                return;
            case "kitt":
                AppData.putCustomData(AppData.kitchentitle, inputedSN);
                showToast("App has switched kitchentitle to: " + inputedSN);
                return;
            case "l":
                AppData.putCustomData(AppData.limitation, inputedSN);
                showToast("App has switched limitation to: " + inputedSN);
                return;
            case "lastchar":
                AppData.putCustomData(AppData.KEY_CUST_LAST_CHAR, inputedSN);
                showToast("Please restart app to apply new layout.");
                return;
            case "layo":
                AppData.putCustomData(AppData.custChars, inputedSN);
                showToast("App has switched custChars to: " + inputedSN);
                return;
            case "m":
                AppData.putCustomData(AppData.mode, inputedSN);
                showToast("App is in switched to mode : " + inputedSN);
                return;
            case "ml":
                AppData.putCustomData(AppData.menuNameLength, inputedSN);
                showToast("App has set menuNameLength to: " + inputedSN);
                return;
            case "pric":
                AppData.putCustomData(AppData.priceonkitchenbill, inputedSN);
                showToast("App has set priceonkitchenbill to: " + inputedSN);
                return;
            case "r":
                AppData.putCustomData(AppData.reportPrinter, inputedSN);
                showToast("reportPrinter is set to : " + inputedSN);
                return;
            case "rf":
                AppData.putCustomData(AppData.reportFont, inputedSN);
                showToast("reportFont is set to : " + inputedSN);
                return;
            case "rw":
                AppData.putCustomData(AppData.reportWidth, inputedSN);
                showToast("reportWidth is set to : " + inputedSN);
                return;
            case "s":
                AppData.putCustomData(AppData.sendReport, inputedSN);
                showToast("App has set sendReport to: " + inputedSN);
                return;
            case "s1":
                AppData.putCustomData(AppData.sep_str1, inputedSN);
                showToast("App has set sep_str1 to: " + inputedSN);
                return;
            case "s2":
                AppData.putCustomData(AppData.sep_str2, inputedSN);
                showToast("App has set sep_str2 to: " + inputedSN);
                return;
            case "sset":
                AppData.putCustomData(AppData.sendOnlyWhenReset, inputedSN);
                showToast("App has set sendOnlyWhenReset to: " + inputedSN);
                return;
            case "surl":
                AppData.putCustomData(AppData.server_url, inputedSN);
                showToast("App has set server_url to: " + inputedSN);
                return;
            case "sip":
                AppData.putCustomData(AppData.serverip, inputedSN);
                showToast("App has set serverip to: " + inputedSN);
                return;
            case "smp":
                AppData.putCustomData(AppData.ShowMarkPirce, inputedSN);
                showToast("App has set ShowMarkPirce to: " + inputedSN);
                return;
            case "star":
                AppData.putCustomData(AppData.userPassword, inputedSN);
                showToast("userPassword is set to: " + inputedSN);
                return;
            case "tpos":
                AppData.putCustomData(AppData.title_position, inputedSN);
                showToast("title_position is set to: " + inputedSN);
                return;
            case "up":
                AppData.putCustomData(AppData.startTime, inputedSN);
                showToast("startTime is set to: " + inputedSN);
                return;
            case "wt":
                AppData.putCustomData(AppData.waitTime, inputedSN);
                showToast("waitTime is set to: " + inputedSN);
                return;
            case "w":
                AppData.putCustomData(AppData.width, inputedSN);
                if("16".equals(inputedSN)){
                    AppData.putCustomData("font", "29, 33, 34");
                }
                showToast("App has set width to: " + inputedSN);
                return;

            default:
                AppData.putCustomData(SettingType, inputedSN);
                showToast(SettingType + " is set to " + inputedSN);
                return;
        }
    }

}
