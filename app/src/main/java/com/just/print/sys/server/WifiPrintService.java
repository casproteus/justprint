package com.just.print.sys.server;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.util.AppUtils;
import com.just.print.util.Command;
import com.just.print.util.L;
import com.zj.wfsdk.WifiCommunication;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiPrintService implements Runnable{
    private final String TAG = "WifiPrintService";

    private HashMap<String,Printer> ipMap;
    private HashMap<String,List<SelectionDetail>> tmpralQueueMap;
    private HashMap<String,List<String>> contentForPrintMap;

    private static String curPrintIp = "";
    private int len_80mm = 24;

    private WifiCommunication wifiCommunication;
    private boolean isConnected;
    private boolean nonEmptyListFound;

    public interface StatusDisplayer {
        void showStatus(String src,int i);
    }
    private StatusDisplayer statusDisplayer = null;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case WifiCommunication.WFPRINTER_CONNECTED:
                    L.d(TAG,"Connected ip#" + WifiPrintService.curPrintIp);
//                    Command.GS_ExclamationMark[2] = 0x11;
//                    wifiCommunication.sndByte(Command.GS_ExclamationMark);//put it here will cause app exit in emulator(5x api 24)
                    isConnected = true;
                    break;
                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    L.d(TAG,"Disconnected");
                    nonEmptyListFound = false;
                    isConnected = false;
                    curPrintIp = "";
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    L.d(TAG,"Connectederr");
                    if(statusDisplayer != null){
                        statusDisplayer.showStatus(curPrintIp,2);
                    }
                    AppUtils.sleep(1000);
                    nonEmptyListFound = false;
                    isConnected = false;
                    break;
                case WifiCommunication.SEND_FAILED:
                    L.d(TAG, "printer message send_failed");
                    nonEmptyListFound = false;
                    isConnected = false;
                    //发送失败对策暂无
                    break;
            }
        }
    };

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    private static WifiPrintService instance = null;
    public static WifiPrintService getInstance(){
        if(instance == null){
            instance = new WifiPrintService();
        }
        return instance;
    }
    private WifiPrintService(){
        wifiCommunication = new WifiCommunication(handler);

        reInitPrintRelatedMaps();

        pool.execute(this);
        L.d(TAG,"Create Service Successful");
    }

    public int registPrintState(final StatusDisplayer ckStatusDisplayer){
        statusDisplayer = ckStatusDisplayer;
        return 0;
    }

    public void reInitPrintRelatedMaps(){
        ipMap = new HashMap<String,Printer>();
        contentForPrintMap = new HashMap<String,List<String>>();
        tmpralQueueMap = new HashMap<String,List<SelectionDetail>>();

        List<Printer> printerList = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getPrinterDao());//读取打印机位置
        for(Printer printer:printerList){
            ipMap.put(printer.getIp(),printer);
            contentForPrintMap.put(printer.getIp(),new ArrayList<String>());
            tmpralQueueMap.put(printer.getIp(),new ArrayList<SelectionDetail>());
        }
    }

    public String exePrintCommand(){
        L.d(TAG,"exePrintCommand");
        if(!isContentForPrintMapEmpty()){
            statusDisplayer.showStatus("current print job not finished yet!",4);
            return "2";                     //未打印完毕
        }

        //1、遍历每个选中的菜，并分别遍历加在其上的打印机。并在queueMap上对应IP后面增加菜品
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            List<M2M_MenuPrint> printerList = selectionDetail.getDish().getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m: printerList) {
                //should never happen, jist in case someone changed db.
                Printer printer = m2m.getPrint();
                if(printer == null) {
                    statusDisplayer.showStatus("Selected item not connected with printer yet.",4);
                    return "2";
                }

                String ip = printer.getIp();
                L.d(TAG,"ip#" + ip + " type:" + printer.getFirstPrint());
                tmpralQueueMap.get(ip).add(selectionDetail);
            }
        }

        //2、遍历queueMap，如对应打印机type为0, 则对其后的value(dishes)按照类别进行排序
        Iterator sortiter = tmpralQueueMap.entrySet().iterator();
        while(sortiter.hasNext()){
            Map.Entry entry = (Map.Entry)sortiter.next();
            String key = (String)entry.getKey();
            List<SelectionDetail> value = (List<SelectionDetail>) entry.getValue();

            L.d(TAG,"ip#" + key + "list size#" + value.size());
            if(ipMap.get(key).getType() == 0 && value.size() > 0){
                //订单排序
                Collections.sort(tmpralQueueMap.get(key), new Comparator<SelectionDetail>() {
                    @Override
                    public int compare(SelectionDetail dishesDetailModel, SelectionDetail t1) {
                        Category c1 = Applic.app.getDaoMaster().newSession().getCategoryDao().load(dishesDetailModel.getDish().getCid());
                        Category c2 = Applic.app.getDaoMaster().newSession().getCategoryDao().load(t1.getDish().getCid());
                        return c1.getDisplayIdx().compareTo(c2.getDisplayIdx());
                    }
                });
            }
        }

        //3、再次遍历queueMap, 封装打印信息
        Iterator iterator = tmpralQueueMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            String printerIP = (String)entry.getKey();
            List<SelectionDetail> dishList = (List<SelectionDetail>) entry.getValue();

            L.d(TAG,"ip#" + printerIP + "list size#" + dishList.size());
            if(dishList.size() > 0){
                if(tmpralQueueMap.get(printerIP) != dishList){
                    System.out.println("Attenttion! the dishList are different from tmpralQueueMap.get(printerIP)!!!!");
                }
                if(ipMap.get(printerIP).getFirstPrint() == 1){  //全单封装
                    contentForPrintMap.get(printerIP).add(formatContentForPrint(dishList));
                }else{                                          //分单封装
                    for(SelectionDetail selectionDetail : dishList){
                        List<SelectionDetail> tlist = new ArrayList<SelectionDetail>();
                        tlist.add(selectionDetail);
                        contentForPrintMap.get(printerIP).add(formatContentForPrint(tlist));
                    }
                }
            }
            //clear the queueMap immediately
            tmpralQueueMap.get(printerIP).clear();
        }

        statusDisplayer.showStatus("SelectionDetail has been send to printer",4);
        return "0";
    }

    public void run(){
        while(true){
            if(false){
                return;
            }

            //do initSocket for the first non-empty entry, (non-empty means the values hidden in this printerIp is non-empty.)
            if(isConnected == false && nonEmptyListFound == false) {
                Iterator iterator = contentForPrintMap.entrySet().iterator();
                while(iterator.hasNext()){

                    Map.Entry entry = (Map.Entry)iterator.next();
                    String printerIP = (String)entry.getKey();
                    List<String> contentList = (List<String>)entry.getValue();
                    if(contentList.size() > 0){
                        nonEmptyListFound = true;

                        L.d(TAG,"ip#" + printerIP + "list size#" + contentList.size());
                        L.d(TAG,"initSocket");
                        wifiCommunication.initSocket(printerIP,9100);
                        curPrintIp = printerIP;
                        break;
                    }
                }
            }

            //if find some "non-empty" value behind any printerIp in the map, and the isConnected is happened set to true then execute print.
            if(nonEmptyListFound == true && isConnected == true){
                if(contentForPrintMap !=null && contentForPrintMap.get(curPrintIp) != null) {
                    L.d(TAG,"Start print");
                    List<String> contentList = contentForPrintMap.get(curPrintIp);
                    for (String content : contentList) {
                        L.d(TAG,"out#");
                        L.d(TAG,content);

                        if(content.length()>0) {
                            wifiCommunication.sndByte(Command.BEEP);
                            Command.GS_ExclamationMark[2] = 0x11;
                            wifiCommunication.sndByte(Command.GS_ExclamationMark);
                            wifiCommunication.sendMsg(content, "GBK");
                            wifiCommunication.sndByte(Command.GS_V_m_n);
                        }
                    }
                    contentForPrintMap.get(curPrintIp).clear();
                    isConnected = false;
                    //close the connectoion afer each print task.
                    wifiCommunication.close();
                }
            }
            AppUtils.sleep(1000);
        }
    }

//    public void closeWifiService(){
//        L.d(TAG,"closeService");
//        if(wifiCommunication !=null) {
//            wifiCommunication.close();
//        }
//    }

    private String formatContentForPrint(List<SelectionDetail> list){
        L.d(TAG,"formatContentForPrint");
        StringBuilder content = new StringBuilder("\n\n");
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date d = new Date();
        String dateStr = df.format(d);
        String spaceStr = generateSpaceString(len_80mm - (CustomerSelection.getInstance().getTableNumber().length() + dateStr.length()));
        content.append(CustomerSelection.getInstance().getTableNumber()).append(spaceStr).append(dateStr);
        content.append("------------------------");
        for(SelectionDetail dd:list){
            content.append(dd.getDish().getID());
            content.append(generateSpaceString(5 - dd.getDish().getID().length()));
            content.append(dd.getDish().getMname());
            if(dd.getDishNum() > 1){
                L.d(TAG,Integer.toString(dd.getDish().getMname().getBytes().length));
                content.append(generateSpaceString(14 - (dd.getDish().getMname().getBytes().length)/3*2)).append("X").append(Integer.toString(dd.getDishNum()));
            }

            content.append("\n");

            for(Mark str:dd.getMarkList()){
                content.append(generateSpaceString(5)).append("* ").append(str.getName()).append(" *\n");
            }
        }
        content.append("\n\n\n\n\n");
        return content.toString();
    }

    private String generateSpaceString(int l){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0;i<l;i++){
            sb.append(" ");
        }
        return sb.toString();
    }

    private boolean isContentForPrintMapEmpty(){
        Iterator iterator = contentForPrintMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            List<String> listTypeValue = (List<String>)entry.getValue();
            if(listTypeValue.size() > 0){
                return false;
            }
        }
        return true;
    }

}
