package com.just.print.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.just.print.app.AppData;
import com.just.print.app.Applic;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Date;

public class DatabaseUtil extends Thread{

    final private static String DATABASE_NAME_PREFFIX = "JustPrinter_"; //data/user/0/com.just.print/databases/ ; /data/data/com.whatsapp/databases/msgstore.db


    private static File file = null;

    /**
     * Call this method from any activity in your app (
     * for example ->    DatabaseUtil.copyDatabaseToExtStg(MainActivity.this);
     * this method will copy the database of your application onto SERVER
     */
    public static void syncDbOntoServer() {
        //https://developer.android.com/reference/android/content/Context.html#getDatabasePath(java.lang.String)
        file = Applic.app.getApplicationContext().getDatabasePath(DATABASE_NAME_PREFFIX + AppData.getShopName(Applic.app.getApplicationContext()));
        if (file.exists()){
            new DatabaseUtil().start();
        }else{
            L.e("DatabaseUtil", "can not find database!", null);
            ToastUtil.showToast(Applic.app.getApplicationContext(), "Can not find the menu file.");
        }
    }

    @Override
    public void run() {
        super.run();
        boolean debug = true;
        if(debug && AppUtils.hasInternet(Applic.app.getApplicationContext())){

            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL("http://192.168.1.14/taostyle/mediauploads");
                urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
                urlConnection.setConnectTimeout(3000);//连接的超时时间
                urlConnection.setUseCaches(false);//不使用缓存
                //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
                urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
                urlConnection.setReadTimeout(3000);//响应的超时时间
                urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
                urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
                urlConnection.setRequestMethod("POST");//设置请求的方式
                urlConnection.setRequestProperty("Content-Type", "text/html");//设置消息的类型
                urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接


                FileInputStream is = new FileInputStream(file);
                byte[] content = toByteArray(file);

                JSONObject json = new JSONObject();//创建json对象
                json.put("filepath", URLEncoder.encode(AppData.getShopName()+AppData.getLicense(), "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("content", content);        //put db into json
                json.put("contentType", "db");
                json.put("submitDate",AppData.getLastSyncDate());
                String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串
                //------------字符流写入数据------------
                OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
                bw.write(jsonstr);//把json字符串写入缓冲区中
                bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                out.close();
                bw.close();//使用完关闭

                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功
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
                    JSONObject rjson = new JSONObject(buffer.toString());

                    Log.d("zxy", "rjson="+rjson);//rjson={"json":true}
                    boolean result = rjson.getBoolean("json");//从rjson对象中得到key值为"json"的数据，这里服务端返回的是一个boolean类型的数据
                    if(result){//判断结果是否正确
//                        mHandler.sendEmptyMessage(USERLOGIN_SUCCESS);
                    }else{
//                        mHandler.sendEmptyMessage(USERLOGIN_FAILED);
                    }
                }else{
//                    mHandler.sendEmptyMessage(USERLOGIN_FAILED);
                }
            } catch (Exception e) {
//                mHandler.sendEmptyMessage(USERLOGIN_FAILED);
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
        }else{
            ToastUtil.showToast(Applic.app.getApplicationContext(), "Can not access Internet, please connect to internet then try again.");
        }
    }

    public static byte[] toByteArray(File file)throws IOException {

        FileChannel fc = null;
        try{
            fc = new RandomAccessFile(file,"r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int)fc.size()];
            if (byteBuffer.remaining() > 0) {
//              System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        }catch (IOException e) {
            e.printStackTrace();
            throw e;
        }finally{
            try{
                fc.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
