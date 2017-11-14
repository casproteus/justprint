package com.just.print_night.util;

import android.util.Base64;

import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;

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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Date;

public class DatabaseUtil extends Thread{

    final private static String DATABASE_NAME_PREFFIX = "JustPrinter_"; //data/user/0/com.just.print/databases/ ; /data/data/com.whatsapp/databases/msgstore.db
    final private static String SERVER_URL = "http://test.sharethegoodones.com";
    //final private static String SERVER_URL = "http://192.168.1.2/taostyle";
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
                StringBuilder sb = new StringBuilder(SERVER_URL);
                sb.append("/syncJustPrintDb?filepath=");
                sb.append(AppData.getLicense() + AppData.getShopName());
                sb.append("&submitDate=");
                sb.append(AppData.getLastSyncDate());
                url = new URL(sb.toString());

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setConnectTimeout(30000);
                urlConnection.setUseCaches(false);
                //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
                urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
                urlConnection.setReadTimeout(30000);//响应的超时时间
                urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
                urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接


                FileInputStream is = new FileInputStream(file);
                byte[] content = toByteArray(file);
                String encodedStr = Base64.encodeToString(content,Base64.DEFAULT);//new String(content);//, "ISO-8859-1");
                if(false){
                    byte[] content2 = encodedStr.getBytes();//"ISO-8859-1");
                    writeDBByte(content2);
                    ToastUtil.showToast("!!!!");
                    return;
                }
                //------------字符流写入数据------------
                OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
                bw.write(encodedStr);//把json字符串写入缓冲区中
                bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                out.close();
                bw.close();//使用完关闭

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功
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
                    if(buffer.length() > 10) {
                        String receivedStr = buffer.toString();
                        byte[] receivedContent = Base64.decode(receivedStr, Base64.DEFAULT);

                        writeDBByte(receivedContent);
                        ToastUtil.showToast("menu in this device has been refreshed successfully.");
                    }else{
                        ToastUtil.showToast("Menu is saved onto server successfully.");
                    }
                }else{
//                    mHandler.sendEmptyMessage(USERLOGIN_FAILED);
                }
            } catch (Exception e) {
                ToastUtil.showToast("Can not connnect with sever, please make sure the device connected with internet first, and try again!");
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

    public static void writeDBByte(byte[] datas) {

        File file = Applic.app.getApplicationContext().getDatabasePath(
                DATABASE_NAME_PREFFIX + AppData.getShopName(Applic.app.getApplicationContext()));
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



    private class MediaUpload {

        private String filepath;

        private String contentType;

        private byte[] content;

        private Date submitDate;

        public String getFilepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public Date getSubmitDate() {
            return submitDate;
        }

        public void setSubmitDate(
                Date submitDate) {
            this.submitDate = submitDate;
        }

    }
}
