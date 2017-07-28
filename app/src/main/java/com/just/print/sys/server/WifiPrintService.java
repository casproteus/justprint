package com.just.print.sys.server;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.just.print.Thread.PrintThreadPool;
import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.DishesDetailModel;
import com.just.print.util.Command;
import com.just.print.util.L;
import com.zj.wfsdk.WifiCommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WifiPrintService implements Runnable{
    private final String TAG = "WifiPrintService";
    private HashMap<String,Printer> ipMap;
    private List<DishesDetailModel> detailList;
    private HashMap<String,List<String>> printMap;
    private HashMap<String,List<DishesDetailModel>> queueMap;
    private static String curPrintIp;
    private String printOut;
    private int n;
    private int len_80mm;
    private WifiCommunication wfComm;
    private boolean isConn;
    private boolean isInit;
    private final byte[][] byteCommands = {
            { 0x1b, 0x42, 0x02, 0x03 },//蜂鸣指令
    };

    public interface PrinterState {
        void ckPrinterState(String src,int i);
    }
    private PrinterState m_ckPrinterState = null;

    private static WifiPrintService instance = null;
    public static WifiPrintService getInstance(){
        if(instance == null){
            instance = new WifiPrintService();
        }
        return instance;
    }

    public WifiPrintService(){
        L.d(TAG,"WifiPrintService");
        isConn = false;
        isInit = false;
        len_80mm = 24;
        wfComm = new WifiCommunication(handler);
        curPrintIp = "";
        n = 1;
        //读取打印机位置
        //map = new HashMap<String,String>();
        ipMap = new HashMap<String,Printer>();
        printMap = new HashMap<String,List<String>>();
        queueMap = new HashMap<String,List<DishesDetailModel>>();
        List<Printer> printerList = DaoExpand.queryNotDeleteAll(Applic.getApp().getDaoMaster().newSession().getPrinterDao());
        reInitPrintRelatedMaps(printerList);
        PrintThreadPool.getInstance().getPrintThreadPool().execute(this);
        printOut = "";
        L.d(TAG,"Create Service Successful");
    }

    public int registPrintState(final PrinterState ckPrinterState){
        m_ckPrinterState = ckPrinterState;
        return 0;
    }

    public int exePrintCommand(List<DishesDetailModel> ddList){
        L.d(TAG,"exePrintCommand");
        printOut = printFormat(ddList);

        //map.put("192.168.1.100",printOut);

        return 0;
    }

    public void reInitPrintRelatedMaps(List<Printer> list){
        ipMap = new HashMap<String,Printer>();
        printMap = new HashMap<String,List<String>>();
        for(Printer printer:list){
            ipMap.put(printer.getIp(),printer);
            printMap.put(printer.getIp(),new ArrayList<String>());
            queueMap.put(printer.getIp(),new ArrayList<DishesDetailModel>());
        }
    }

    public String exePrintCommand(){
        L.d(TAG,"exePrintCommand");
        String tmpAllmenuContents;
        if(!isListInPrintMapEmpty()){
            m_ckPrinterState.ckPrinterState("current print job not finished yet!",4);
            return "2";                     //未打印完毕
        }
        //添加菜品信息至打印队列
        for(DishesDetailModel ddm : MenuService.getInstance().getMenu()){
            //1、添加菜单至打印队列

            for(M2M_MenuPrint m2m:ddm.getDish().getM2M_MenuPrintList()) {
                if(m2m.getPrint() == null) {
                    m_ckPrinterState.ckPrinterState("选择菜品未绑定打印机",4);
                    return "2";
                }
                String ip = m2m.getPrint().getIp();
                Integer type = m2m.getPrint().getFirstPrint();
                L.d(TAG,"ip#" + ip + " type:" + type);
                queueMap.get(ip).add(ddm);
            }
        }
        //2、遍历打印机，对打印机按照打印种类进行排序
        //初始化打印队列

        Iterator sortiter = queueMap.entrySet().iterator();
        while(sortiter.hasNext()){

            Map.Entry entry = (Map.Entry)sortiter.next();
            String key = (String)entry.getKey();
            List<DishesDetailModel> value = (List<DishesDetailModel>) entry.getValue();
            L.d(TAG,"ip#" + key + "list size#" + value.size());
            if(ipMap.get(key).getType() == 0 && value.size() > 0){
                //订单排序
                Collections.sort(queueMap.get(key), new Comparator<DishesDetailModel>() {
                    @Override
                    public int compare(DishesDetailModel dishesDetailModel, DishesDetailModel t1) {

                        Category c1 = Applic.getApp().getDaoMaster().newSession().getCategoryDao().load(dishesDetailModel.getDish().getCid());
                        Category c2 = Applic.getApp().getDaoMaster().newSession().getCategoryDao().load(t1.getDish().getCid());
                        return c1.getCname().compareTo(c2.getCname());
                    }
                });
            }
        }
        //3、封装打印信息
        Iterator pntiter = queueMap.entrySet().iterator();
        while(pntiter.hasNext()){

            Map.Entry entry = (Map.Entry)pntiter.next();
            String key = (String)entry.getKey();
            List<DishesDetailModel> value = (List<DishesDetailModel>) entry.getValue();
            L.d(TAG,"ip#" + key + "list size#" + value.size());
            if(value.size() > 0){
                if(ipMap.get(key).getFirstPrint() == 1){
                    //全单封装
                    printMap.get(key).add(printFormat(queueMap.get(key)));
                }
                else{
                    //分单封装
                    for(int i = 0;i < queueMap.get(key).size();i++){
                        List<DishesDetailModel> tlist = new ArrayList<DishesDetailModel>();
                        tlist.add(queueMap.get(key).get(i));
                        printMap.get(key).add(printFormat(tlist));
                    }
                }
            }
            //清空打印队列
            queueMap.get(key).clear();
        }
        m_ckPrinterState.ckPrinterState("打印订单已生成",4);
        return "0";
    }

    public void run(){
        while(true){
            if(true){
                return;
            }

            //打印机遍历打印机
            if(isConn != true && isInit != true) {
                Iterator iter = printMap.entrySet().iterator();
                while(iter.hasNext()){

                    Map.Entry entry = (Map.Entry)iter.next();
                    String key = (String)entry.getKey();
                    List<String> value = (List<String>)entry.getValue();
                    if(value.size() > 0){
                        L.d(TAG,"ip#" + key + "list size#" + value.size());
                        L.d(TAG,"initSocket");
                        curPrintIp = key;
                        wfComm.initSocket(key,9100);
                        isInit = true;
                    }
                }
            }

            //执行打印处理
            if(isInit == true && isConn == true){

                if(printMap!=null && printMap.get(curPrintIp) != null) {
                    L.d(TAG,"Start print");
                    List<String> tmplist = printMap.get(curPrintIp);
                    for (String out : tmplist) {
                        L.d(TAG,"out#");
                        L.d(TAG,out);
                        SendDataByte(byteCommands[0]);
                        SendDataString(out);
                        SendDataByte(Command.GS_V_m_n);
                    }
                    printMap.get(curPrintIp).clear();

                    //打印完毕后切断连接
                    wfComm.close();
                    isConn = false;
                }
            }
            sleep(1000);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case WifiCommunication.WFPRINTER_CONNECTED:
                    L.d(TAG,"Connected ip#" + WifiPrintService.curPrintIp);
                    isConn = true;
                    Command.GS_ExclamationMark[2] = 0x11;
                    SendDataByte(Command.GS_ExclamationMark);

                    break;
                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    //isConn = false;
                    L.d(TAG,"Disconnected");
                    isInit = false;
                    isConn = false;
                    curPrintIp = "";
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    L.d(TAG,"Connectederr");
                    if(m_ckPrinterState != null){
                        m_ckPrinterState.ckPrinterState(curPrintIp,2);
                    }
                    sleep(1000);
                    isInit = false;
                    break;
                case WifiCommunication.SEND_FAILED:


                    //发送失败对策暂无
                    break;
            }
        }
    };

    private void SendDataString(String data){
        if(data.length()>0)
            wfComm.sendMsg(data, "GBK");
            //wfComm2.sendMsg("no2" + data, "GBK");
    }

    public void closeWifiService(){
        L.d(TAG,"closeService");
        if(wfComm!=null) {
            wfComm.close();
        }
    }

    public String printFormat(List<DishesDetailModel> list){
        L.d(TAG,"printFormat");
        String out = "\n\n";
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date d = new Date();
        String dateStr = df.format(d);
        String spaceStr = spaceFormat(len_80mm - (MenuService.getInstance().getTableNum().length() + dateStr.length()));
        out += MenuService.getInstance().getTableNum() + spaceStr + dateStr;
        out +="------------------------";
        for(DishesDetailModel dd:list){
            out += dd.getDish().getID();
            out += spaceFormat(5 - dd.getDish().getID().length());
            out += dd.getDish().getMname();
            if(dd.getDishNum() > 1){
                L.d(TAG,Integer.toString(dd.getDish().getMname().getBytes().length));
                out += spaceFormat(14 - (dd.getDish().getMname().getBytes().length)/3*2) + "X" + Integer.toString(dd.getDishNum()) + "\n";
            }
            else{
                out += "\n";
            }


            for(Mark str:dd.getMarkList()){
                out += spaceFormat(5) + "** " + str.getName() + " **\n";
            }
        }
        out +="\n\n\n\n\n";
        return out;
    }

    public String spaceFormat(int l){
        String sp = "";
        for (int i = 0;i<l;i++){
            sp +=" ";
        }
        return sp;
    }

    private void SendDataByte(byte[] data){
        if(data.length>0){
            wfComm.sndByte(data);
        }
    }

    private boolean isListInPrintMapEmpty(){
        Iterator iter = printMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            List<String> listTypeValue = (List<String>)entry.getValue();
            if(listTypeValue.size() > 0){
                return false;
            }
        }
        return true;
    }

    private void sleep(int time){
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
