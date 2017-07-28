package com.just.print.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.just.print.sys.ServiceExecutor;
import com.just.print.sys.model.AbsModel;
import com.just.print.sys.model.AbsRequest;
import com.just.print.sys.model.AbsResult;
import com.just.print.util.AppUtils;
import com.just.print.util.L;
import com.stupid.method.http.IXHttp;
import com.stupid.method.http.impl.asyncHttp.XAsyncIXHttp;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

/**
 * Created by wangx on 2016/10/28.
 *
 * @protocol UDP-JSON
 */
public class UDPService extends Service implements TCPSever.TcpServerReadOver {
    private static final int PORT = 8181;
    private static final int HTTP_PORT = 8182;
    ExecutorService timeoutExecutor = Executors.newSingleThreadExecutor();
    protected final EventLoopGroup group = new NioEventLoopGroup(10);
    Channel channel;
    WeakHashMap<Context, String> l;
    String deviceID;
    IXHttp http = new XAsyncIXHttp();

    @Override
    public void onReadOver(String host, byte[] input) {
        //业务
        String response = null;
        try {
            response = new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (response == null)
            return;
        JSONObject json = JSONObject.parseObject(response);
        Class<? extends AbsModel> clz =
                AbsModel.getChildClassOrLoad(json.getString("CLASS_NAME"));
        if (clz != null) { //业务
            AbsModel absModel = json.toJavaObject(clz);
            synchronized (sparseArray) {
                if (sparseArray.get(absModel.getRequestID()) != null) {
                    if (sparseArray.get(absModel.getRequestID()).onCallback(true, absModel.getRequestCode(),
                            absModel))//如果不在响应该回调,就移出
                        sparseArray.remove(absModel.getRequestID());
                }
            }
        }
    }

    public interface UDPCallback {
        boolean onCallback(boolean status, int requestCode, AbsModel result);
    }

    SparseArray<UDPCallback> sparseArray = new SparseArray<>();
    static int requestCount = 100;

    public void sendResult(AbsRequest request, AbsResult result, InetSocketAddress address) {
        result.deviceID = deviceID;
        result.setRequestCode(request.getRequestCode());
        result.setRequestID(request.getRequestID());
        try {
            TcpClient tcpClient = new TcpClient(address.getHostName(), HTTP_PORT, result.toString().getBytes("UTF-8"));
            group.execute(tcpClient);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void send(String data, InetSocketAddress address) throws InterruptedException {
        channel.writeAndFlush(
                new DatagramPacket(Unpooled.copiedBuffer(data.toString(),
                        CharsetUtil.UTF_8), address)).sync();
    }

    public void sendRequest(final AbsRequest msg, final int requestCode, UDPCallback callback) {
        try {
            msg.deviceID = deviceID;
            msg.setRequestCode(requestCode);
            msg.setRequestID(requestCount++);
            sparseArray.append(msg.getRequestID(), callback);
            timeoutExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    L.d(tag, "请求超时");
                    synchronized (sparseArray) {
                        UDPCallback callback = sparseArray.get(msg.getRequestID());
                        if (callback != null) {
                            callback.onCallback(false, requestCode, null);
                            sparseArray.remove(msg.getRequestID());
                        }
                    }
                }
            });
            send(msg.toString(), new InetSocketAddress("255.255.255.255", PORT));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    TCPSever tcpSever = new TCPSever(HTTP_PORT, this, group);

    @Override
    public void onCreate() {
        super.onCreate();
        deviceID = AppUtils.getDeviceID(this);

        group.execute(new Runnable() {
            @Override
            public void run() {
                tcpSever.start();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, true).handler(new ChHandler());
                try {
                    channel = bootstrap.bind(PORT).sync().channel();
                    channel.closeFuture().await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void registerListening() {

    }

    private static final String tag = "UDT.SERVICE";


    private class ChHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ch, DatagramPacket msg) throws Exception {

            String response = msg.content().toString(CharsetUtil.UTF_8);
            L.d(tag, response);
            try {
                // byte[] res = Base64.decode(response.getBytes(), Base64.NO_PADDING);//数据转码
                //response = new String(res);
                JSONObject json = JSONObject.parseObject(response);
                if (json.getString("deviceID") == null
                        || (json.getString("deviceID") != null && json.getString("deviceID").equals(deviceID))) {
                    L.d(tag, "自己发的,无视..");
                    return;
                }
                Class<? extends AbsModel> clz =
                        AbsModel.getChildClassOrLoad(json.getString("CLASS_NAME"));
                if (clz != null) {
                    AbsModel absModel = JSON.parseObject(response, clz);
                    if (absModel != null) {
                        if (absModel.getType() == 1) {
                            ServiceExecutor.execute(absModel, UDPService.this, msg.sender());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {

            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public UDPService getService() {
            return UDPService.this;
        }
    }

}
