package com.just.print.sys.server;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.util.AppUtils;
import com.just.print.util.Command;
import com.just.print.util.L;
import com.just.print.util.ToastUtil;
import com.zj.wfsdk.WifiCommunication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
    private int paperWidth = 24;

    private WifiCommunication wifiCommunication;
    private boolean isConnected;
    private boolean nonEmptyListFound;

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
                    showStatus(curPrintIp,2);

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
            showStatus("current print job not finished yet!",4);
            return "2";                     //未打印完毕
        }

        //1、遍历每个选中的菜，并分别遍历加在其上的打印机。并在queueMap上对应IP后面增加菜品
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            List<M2M_MenuPrint> printerList = selectionDetail.getDish().getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m: printerList) {
                //should never happen, jist in case someone changed db.
                Printer printer = m2m.getPrint();
                if(printer == null) {
                    showStatus("Selected item not connected with printer yet.",4);
                    return "2";
                }

                String ip = printer.getIp();
                L.d(TAG,"ip#" + ip + " type:" + printer.getFirstPrint());
                tmpralQueueMap.get(ip).add(selectionDetail);
            }
        }

        //2、遍历queueMap，如对应打印机type为0, 则对其后的value(dishes)按照类别进行排序
        for(Map.Entry entry: tmpralQueueMap.entrySet()){
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
        for(Map.Entry entry: tmpralQueueMap.entrySet()){
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

        showStatus("SelectionDetail has been send to printer",4);
        return "0";
    }

    //The start time and end time are long format, need to be translate for print.
    public String exePrintReportCommand(List<SaleRecord> saleRecords, String startTime, String endTime){
        L.d("ConfigPrintReportFragment","exePrintCommand");
        List<Printer> printers = Applic.app.getDaoMaster().newSession().getPrinterDao().loadAll();
        String printerIP = printers.get(0).getIp();

        HashMap<String, SaleRecord> map = new HashMap<String, SaleRecord>();
        //combine the records
        for(SaleRecord saleRecord : saleRecords){
            SaleRecord exist = map.get(saleRecord.getMname());
            if(exist == null){
                map.put(saleRecord.getMname(), saleRecord);
            }else{
                exist.setNumber(exist.getNumber() + saleRecord.getNumber());
                exist.setPrice(exist.getPrice() + saleRecord.getPrice());
            }
        }
        List<SaleRecord> combinedSaleRecords = new ArrayList<>();
        for(Map.Entry<String, SaleRecord> entry :map.entrySet()){
            combinedSaleRecords.add(entry.getValue());
        }

        contentForPrintMap.get(printerIP).add(formatContentForPrintReport(combinedSaleRecords, startTime, endTime));

        ToastUtil.showToast("salesReport has been send to printer: " + printerIP);
        return "0";
    }

    public void run(){
        while(true){

            //do initSocket for the first non-empty entry, (non-empty means the values hidden in this printerIp is non-empty.)
            if(isConnected == false && nonEmptyListFound == false) {
                for(Map.Entry entry : contentForPrintMap.entrySet()){
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
                            if(!"silent".equals(AppData.getCustomData("mode"))) {
                                wifiCommunication.sndByte(Command.BEEP);
                            }
                            String version = AppData.getCustomData("version");
                            if("bigger".equals(AppData.getCustomData("version"))) {
                                Command.GS_ExclamationMark[2] = (byte) (((int)Command.GS_ExclamationMark[2]) * 3);
//                                Command.GS_ExclamationMark[5] = (byte) (((int)Command.GS_ExclamationMark[2]) * 3);
//                                Command.GS_ExclamationMark[8] = (byte) (((int)Command.GS_ExclamationMark[2]) * 3);
                            }
                            wifiCommunication.sndByte(Command.GS_ExclamationMark);
                            wifiCommunication.sendMsg(content, "GBK");//"UTF-8");
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

    private String formatContentForPrintReport(List<SaleRecord> saleRecords, String startTime, String endTime) {
        L.d(TAG,"formatContentForPrintReport");

        //translate the time format
        DateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        Date d = new Date(Long.valueOf(startTime));
        startTime = df.format(d);
        d = new Date(Long.valueOf(endTime));
        endTime = df.format(d);

        String spaceStr = generateSpaceString((paperWidth - startTime.length())/2);

        StringBuilder content = new StringBuilder("\n");
        content.append(generateSpaceString((paperWidth - 6)/2));
        content.append("REPORT");
        content.append("\n\n\n");
        content.append(startTime).append(" to");
        content.append("\n");
        content.append(endTime);
        content.append("\n");
        content.append("▂▂▂▂▂▂▂▂▂▂▂▂\n\n");
        Double total = Double.valueOf(0);
        int item = 0;
        for(SaleRecord saleRecord:saleRecords){
            String name = saleRecord.getMname();
            content.append(name);

            int lengthOfName = getLengthOfString(name);
            StringBuilder content2 = new StringBuilder();
            if(lengthOfName < 11){
                content2.append(generateSpaceString(12 - lengthOfName));//x appear at the position of 13
            }else{
                content2.append(" ");
            }

            String number = String.valueOf(saleRecord.getNumber().intValue());
            content2.append("x");
            content2.append(number);
            content.append(content2);

            String price = String.format("%.2f", saleRecord.getPrice());//String.valueOf(((int)(saleRecord.getPrice() * 100))/100.0);

            int spaceLeft = paperWidth - (lengthOfName + content2.length() + price.length() + 1);
            if(spaceLeft < 2){
                content.append(" ");
            }else{
                content.append(generateSpaceString(spaceLeft));
            }

            content.append("$");
            content.append(price);
            content.append("\n");

            item += Integer.valueOf(number);
            total += Double.valueOf(saleRecord.getPrice());
        }
        content.append("────────────\n");
        content.append(item);
        content.append(" ITEMS");

        String totalStr = String.format("%.2f", total);
        String space = generateSpaceString(paperWidth - 7 - String.valueOf(item).length() - totalStr.length());
        content.append(space);

        content.append("$");
        content.append(totalStr);
        //content.append(generateSpaceString(5)).append("* ").append(str.getName()).append(" *\n");
        content.append("\n\n\n\n\n");
        return content.toString();
    }

    private String formatContentForPrint(List<SelectionDetail> list){
        L.d(TAG,"formatContentForPrint");
        boolean needBigger = "bigger".equals(AppData.getCustomData("version"));
        if(needBigger){
            paperWidth = 16;
        }
        StringBuilder content = new StringBuilder("\n\n");
        DateFormat df = new SimpleDateFormat("HH:mm");
        String tableName = CustomerSelection.getInstance().getTableNumber();
        String dateStr = df.format(new Date());
        String spaceStr = generateSpaceString(paperWidth - (2 + CustomerSelection.getInstance().getTableNumber().length() + dateStr.length()));

        if(needBigger){
            content.append("\n\n");
        }

        content.append("(").append(tableName).append(")").append(spaceStr).append(dateStr).append("\n");

        content.append(needBigger ? "▂▂▂▂▂▂▂▂\n\n" : "▂▂▂▂▂▂▂▂▂▂▂▂\n");

        for(SelectionDetail dd:list){
            StringBuilder sb = new StringBuilder();
            sb.append(dd.getDish().getID());
            sb.append(generateSpaceString(5 - dd.getDish().getID().length()));
            sb.append(dd.getDish().getMname());
            if(dd.getDishNum() > 1){
                String space = " ";
                int occupiedLength = getLengthOfString(sb.toString());
                sb.append(generateSpaceString(paperWidth - occupiedLength - (dd.getDishNum() < 10 ? 2 : 3)));
                sb.append("X").append(Integer.toString(dd.getDishNum()));
            }
            content.append(sb);
            content.append("\n");
            if(dd.getMarkList() != null) {
                for (Mark str : dd.getMarkList()) {
                    content.append(generateSpaceString(5)).append("* ").append(str.getName()).append(" *\n");
                }
            }
            content.append(needBigger ? "────────\n" :"────────────\n");
        }
        return content.substring(0, content.length() - (needBigger ? 9 : 13)) + "\n\n\n\n\n";
    }

    private int getLengthOfString(String content){
        int length = content.length();
        int realWidth = length;
        for(int i = 0; i < length; i++) {
            char c = content.charAt(i);
            if(c >=19968 && c <= 171941) {
                realWidth++;
            }
        }
        return realWidth;
    }

    private String generateSpaceString(int l){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0;i<l;i++){
            sb.append(" ");
        }
        return sb.toString();
    }

    private boolean isContentForPrintMapEmpty(){
        for(Map.Entry entry : contentForPrintMap.entrySet()){
            List<String> listTypeValue = (List<String>)entry.getValue();
            if(listTypeValue.size() > 0){
                return false;
            }
        }
        return true;
    }

    private void showStatus(String src, int i) {
        switch (i) {
            case 2:     //WFPRINTER_CONNECTEDERR
                ToastUtil.showToast("Printer:" + src + " connection error!");
                break;
            case 4:     //COMMON
                ToastUtil.showToast(src);
        }

    }
}
