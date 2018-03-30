package com.just.print.sys.server;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSWIFIAPI;
import POSSDK.POSSDK;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import android_wifi_api.SearchPortInfo;

import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.just.print.util.AppUtils;
import com.just.print.util.Command;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
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

    public static String SUCCESS = "0";
    public static String ERROR = "2";

    private HashMap<String,Printer> ipPrinterMap;
    private HashMap<String,List<SelectionDetail>> ipSelectionsMap;
    private HashMap<String,List<String>> ipContentMap;

    private static String curPrintIp = "";
    private int width = 24;
    private String code = "GBK";
    private String SEP_STR1 = "=";
    private String SEP_STR2 = "-";


    private WifiCommunication wifiCommunication;
    private boolean printerConnectedFlag;
    private boolean contentReadyForPrintFlag;

    //fro BeiYangPrinter----------------------

    //WIFI port variable
    private static final int POSPORT = 9100; 	// The port handle of Processing instruction
    private static final int STATEPORT = 4000; 	// The port handle of Query State

    //Print Mode
    private static final int PRINT_MODE_STANDARD = 0;
    private static final int PRINT_MODE_PAGE = 1;
    public static int printMode = PRINT_MODE_STANDARD;

    public static POSSDK pos_wifi = null;
    private POSSDK pos_state = null;
    private POSInterfaceAPI interface_wifi = null;
    private POSInterfaceAPI	interface_state = null;

    private static final int SearchPortMAX = 5;
    private SearchPortInfo port_info[] = new SearchPortInfo[SearchPortMAX];
    private int return_code = 0;
    private String beiYangPrinters = "";

    public static final int POS_SUCCESS=1000;		//success
    public static final int ERR_PROCESSING = 1001;	//processing error
    public static final int ERR_PARAM = 1002;		//parameter error

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){//this method will handle the message queue of this thread base on the message type(.waht)
            switch (msg.what){
                case WifiCommunication.WFPRINTER_CONNECTED:
                    L.d(TAG,"connection generated with ip:" + WifiPrintService.curPrintIp);
//                    Command.GS_ExclamationMark[2] = 0x11;
//                    wifiCommunication.sndByte(Command.GS_ExclamationMark);//put it here will cause app exit in emulator(5x api 24)
                    printerConnectedFlag = true;
                    break;
                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    L.d(TAG,"connection stopped with ip:" + WifiPrintService.curPrintIp);
                    contentReadyForPrintFlag = false;
                    printerConnectedFlag = false;
                    curPrintIp = "";
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    L.d(TAG,"Connection Error! With ip:" + WifiPrintService.curPrintIp);
                    ToastUtil.showToast("Printer:" + curPrintIp + " connection error!");

                    AppUtils.sleep(1000);

                    contentReadyForPrintFlag = false;
                    printerConnectedFlag = false;

                    break;
                case WifiCommunication.SEND_FAILED:
                    L.d(TAG, "ERROR! When sending msg to: " + WifiPrintService.curPrintIp);
                    contentReadyForPrintFlag = false;
                    printerConnectedFlag = false;
                    //发送失败对策暂无
                    break;
                case WifiCommunication.WFPRINTER_REVMSG:
                    L.d(TAG, "got message from server, msg =: " + msg);
                    ToastUtil.showToast("msg:" + msg);
                    break;
            }
        }
    };

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        executorService.execute(this);
        L.d(TAG,"Create Service Successful");
    }

    public void reInitPrintRelatedMaps(){
        ipPrinterMap = new HashMap<String,Printer>();
        ipContentMap = new HashMap<String,List<String>>();
        ipSelectionsMap = new HashMap<String,List<SelectionDetail>>();

        List<Printer> printerList = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getPrinterDao());//读取打印机位置
        for(Printer printer:printerList){
            ipPrinterMap.put(printer.getIp(),printer);
            ipContentMap.put(printer.getIp(),new ArrayList<String>());
            ipSelectionsMap.put(printer.getIp(),new ArrayList<SelectionDetail>());
        }

        beiYangPrinters = prepareBeiYangPrinterStr();
    }

    public String exePrintCommand(){
        L.d(TAG,"start to translate selection into ipContent for printing.");
        if(!isIpContentMapEmpty()){
            L.d(TAG,"ipContent not empty, means last print job not finished yet! returning not success flag.");
            return ERROR;                     //未打印完毕
        }


        //1、遍历每个选中的菜，并分别遍历加在其上的打印机。并在ipSelectionsMap上对应IP后面增加菜品
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            List<M2M_MenuPrint> printerList = selectionDetail.getDish().getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m: printerList) {
                Printer printer = m2m.getPrint();
                if(printer == null) {                   //should never happen, jist in case someone changed db.
                    ToastUtil.showToast("Selected dish not connected with any printer yet.");
                    return ERROR;
                }

                String ip = printer.getIp();
                L.d(TAG,"Adding a dish to ipSelectionsMap, ip:" + ip);
                ipSelectionsMap.get(ip).add(selectionDetail);
            }
        }

        //2、遍历ipSelectionsMap，如对应打印机type为0, 则对其后的value(dishes)按照类别进行排序
        L.d(TAG,"checking how many dishes under each printer...");
        for(Map.Entry entry: ipSelectionsMap.entrySet()){
            String key = (String)entry.getKey();
            List<SelectionDetail> dishList = (List<SelectionDetail>) entry.getValue();

            L.d(TAG,"ip:" + key + ", list size:" + dishList.size());
            if(ipPrinterMap.get(key).getType() == 0 && dishList.size() > 0){
                //订单排序
                Collections.sort(ipSelectionsMap.get(key), new Comparator<SelectionDetail>() {
                    @Override
                    public int compare(SelectionDetail dishesDetailModel, SelectionDetail t1) {
                        Category c1 = Applic.app.getDaoMaster().newSession().getCategoryDao().load(dishesDetailModel.getDish().getCid());
                        Category c2 = Applic.app.getDaoMaster().newSession().getCategoryDao().load(t1.getDish().getCid());
                        return c1.getDisplayIdx().compareTo(c2.getDisplayIdx());
                    }
                });
            }
        }

        //3、再次遍历ipSelectionsMap, 封装打印信息
        for(Map.Entry entry: ipSelectionsMap.entrySet()){
            String printerIP = (String)entry.getKey();
            List<SelectionDetail> dishList = (List<SelectionDetail>) entry.getValue();

            if(dishList.size() > 0){
                if(ipSelectionsMap.get(printerIP) != dishList){
                    L.d("ERROR!", "the dishList are different from ipSelectionsMap.get(printerIP)!!!!");
                }
                if(ipPrinterMap.get(printerIP).getFirstPrint() == 1){  //全单封装
                    ipContentMap.get(printerIP).add(formatContentForPrint(dishList) + "\n\n\n\n\n");
                }else{                                          //分单封装
                    for(SelectionDetail selectionDetail : dishList){
                        List<SelectionDetail> tlist = new ArrayList<SelectionDetail>();
                        tlist.add(selectionDetail);
                        ipContentMap.get(printerIP).add(formatContentForPrint(tlist) + "\n\n");
                    }
                }
            }
            //clear the ipSelectionsMap immediately
            ipSelectionsMap.get(printerIP).clear();
        }

        L.d(TAG, "Order is translated into ipContentMap map and ready for print.");
        ToastUtil.showToast("PRINTING...");
        return SUCCESS;
    }

    //The start time and end time are long format, need to be translate for print.
    public String exePrintReportCommand(List<SaleRecord> saleRecords, String startTime, String endTime){
        L.d("ConfigPrintReportFragment","exePrintCommand");
        List<Printer> printers = Applic.app.getDaoMaster().newSession().getPrinterDao().loadAll();
        String printerIP = AppData.getCustomData("reportPrinter");
        if(printerIP == null || printerIP.length() < 8 || printerIP.indexOf(".") < 1 || printerIP.indexOf(".") > 3) {
            printerIP = printers.get(0).getIp();
        }

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

        ipContentMap.get(printerIP).add(formatContentForPrintReport(combinedSaleRecords, startTime, endTime));

        ToastUtil.showToast("PRINTING REPORT ON " + printerIP);
        return SUCCESS;
    }

    private boolean isReadyToInitNewPrintJob(){
        return contentReadyForPrintFlag == false && printerConnectedFlag == false;
    }

    private boolean isBeiYangPrinter(String printerIP){
        return beiYangPrinters.contains(printerIP);
    }

    private void connectToThePrinter(String printerIP){

        if(isBeiYangPrinter(printerIP)){

            return_code = interface_wifi.OpenDevice(printerIP, POSPORT);
            if(return_code != POS_SUCCESS){
                L.e(TAG, "Open PosPort Failed", null);
                ToastUtil.showToast("Open PosPort Failed. Check Printer!");
                return;
            }else{
                if(pos_wifi != null){
                    pos_wifi = null;
                }
                pos_wifi = new POSSDK(interface_wifi);
            }

            return_code = interface_state.OpenDevice(printerIP, STATEPORT);
            if(return_code != POS_SUCCESS){
                L.e(TAG, "STATEPORT Open Failed!", null);
                ToastUtil.showToast("State Port Open Failed! Check Printer!");
            }else{
                if(pos_state != null){
                    pos_state = null;
                }
                pos_state = new POSSDK(interface_state);
            }

            if (return_code != POS_SUCCESS) {
                ToastUtil.showToast("Failed to connect device, please try again.");
            }

        }else {
            wifiCommunication.initSocket(printerIP, POSPORT);   //this should result in the "printerConnectedFlag" become true .
        }
    }

    private boolean isReadyToPrint(){
        return contentReadyForPrintFlag == true && printerConnectedFlag == true;
    }

    private void printContent(String content){
        //check if it's nbna printer, check printer status
        if(isBeiYangPrinter(curPrintIp)){

            TestPrintInfo testprint = new TestPrintInfo();
            int FontStyle = 0;

            //Get FontStyle
            //if(TogStyleReverse == true){ FontStyle |= 0x400; }  //choose StyleReverse
            //if(TogStyleBold == true){ FontStyle |= 0x08; }      //choose StyleBold
            //if(TogStyleUnderline == true){ FontStyle |= 0x80; } //choose StyleUnderline

            int FontType = 0; //Get FontType
            int Alignment = 0;//Get Alignmenttype
            int HorStartingPosition = 100;//Get HorStartingPosition
            int VerStartingPosition = 20;//Get VerStartingPosition
            int LineHeight = 10;//Get LineHeight
            int HorizontalTimes = 6;//Get HorizontalTimes
            int VerticalTimes = 6;//Get VerticalTimes

            return_code = testprint.TestPrintText(pos_wifi, printMode, content, content.length(), FontType, FontStyle,
                    Alignment,HorStartingPosition, VerStartingPosition, LineHeight, HorizontalTimes, VerticalTimes);
            if(return_code != POS_SUCCESS){
                ToastUtil.showToast("Failed to print Text.");
            }
        }else {
            if (!"silent".equals(AppData.getCustomData("mode"))) {
                wifiCommunication.sndByte(Command.BEEP);
            }
            String font = AppData.getCustomData(curPrintIp + "font");
            if (StringUtils.isBlank(font)) {
                font = AppData.getCustomData("font");
            }
            if (StringUtils.isBlank(font)) {
                wifiCommunication.sndByte(Command.GS_ExclamationMark);
            } else {
                //default: "27, 33, 48" because it works for both thermal and non-thermal
                String[] pieces = font.split(",");
                if (pieces.length != 3) {
                    wifiCommunication.sndByte(Command.GS_ExclamationMark);
                } else {
                    for (int i = 0; i < 3; i++) {
                        Command.GS_ExclamationMark[i] = Integer.valueOf(pieces[i].trim()).byteValue();
                    }
                    wifiCommunication.sndByte(Command.GS_ExclamationMark);
                }
            }

            //code can be customzed
            String tCode = AppData.getCustomData("code");
            if (tCode != null && tCode.length() > 2) {
                code = tCode;
            }
            wifiCommunication.sendMsg(content, code);

            //cut the paper.
            wifiCommunication.sndByte(Command.GS_V_m_n);

            //close the connectoion afer each print task.
            wifiCommunication.close();
        }
    }

    public void run(){

        int timeCounter = 0;
        while(true){
            //check if this round is a good time to reset a curPrintIp and load new contnet to print.
            if(isReadyToInitNewPrintJob()) {
                L.d(TAG,"loading bullet(changing current ip and setup two flags....");
                //stop at the first non-empty entry, (non-empty means the values hidden in this printerIp is non-empty.)
                //and initSocket to the printer key pointing to.
                for(Map.Entry entry : ipContentMap.entrySet()){

                    List<String> contentList = (List<String>)entry.getValue();

                    if(contentList.size() > 0){
                        contentReadyForPrintFlag = true; //mark that we have found something to print, don't come into here and do init socket any more.
                        curPrintIp = (String)entry.getKey();
                        L.d(TAG,"ip changed to:" + curPrintIp + "the size of content to print is:" + contentList.size());
                        L.d(TAG,"contentReadyForPrintFlag setted up! now waiting for initSocket to set up the printerConnectedFlag");

                        connectToThePrinter(curPrintIp); //if success, an other flag (printerConnectedFlag) will be set up
                        break;  //stop for getting content for other printers, stop here, when one printer finished, open connection to an other printer and print again.
                    }
                }

            }else if(isReadyToPrint()){//chick if this round is a good time to do actual print work?

                L.d(TAG,"Flags both set, checking the content fllowing current ip:" + curPrintIp);

                if(ipContentMap !=null && ipContentMap.get(curPrintIp) != null) {

                    List<String> contentList = ipContentMap.get(curPrintIp); L.d(TAG,"out printing... content list size is:" + contentList.size());
                    for (String content : contentList) {    //might print several times, if the printer is setted as "DanDa"
                        if(content.length()>0) {
                            printContent(content);
                        }
                    }

                    //reset status and get ready for a new print job( a print job = connecting to a printer + print content + reset)
                    ipContentMap.get(curPrintIp).clear();
                    printerConnectedFlag = false;
                    contentReadyForPrintFlag = false;

                    isAllPrintedCheck();
                    L.d(TAG,"Print complete (ipcontent cleaned, flag set to false, connection closed!) for ip:" + curPrintIp);
                }else{
                    L.e(TAG,"Unexpected empty Content found when printing to printer: :" + curPrintIp, null);
                    ToastUtil.showToast("Unexpected empty Content found! when printing to printer: " + curPrintIp);
                }
            }else{
                L.d(TAG,"printerConnectedFlag:" + printerConnectedFlag);
                timeCounter++;
                if(timeCounter == 5){
                    timeCounter = 0;
                    ToastUtil.showToast("Printer Error! Check " + curPrintIp);
                }
            }

            //did any work or didn't do any work, each round should rest for 1 second.
            AppUtils.sleep(1000);
        }
    }

    private String prepareBeiYangPrinterStr(){
        String beiYangPrinter = AppData.getCustomData("BeiYangPrinter");
        if(beiYangPrinter != null && beiYangPrinter.length() > 7){
            return beiYangPrinter;
        }

        beiYangPrinter = "";
        for(int i = 0; i < SearchPortMAX; i++){
            port_info[i] = new SearchPortInfo();
        }
        if(interface_wifi == null){
            interface_wifi  = new POSWIFIAPI();
            interface_state = new POSWIFIAPI();
        }

        L.d(TAG, "Searching devices");
        int sch_prt_num = interface_wifi.WIFISearchPort(port_info, SearchPortMAX);
        if(sch_prt_num <= 0){
            L.d(TAG, "No BeiYang WIFI Printer devices found");
        }else{
            for(int i = 0; i < sch_prt_num; i++){
                if (beiYangPrinter.indexOf(port_info[i].GetIPAddress()) == -1)
                    beiYangPrinter.concat("," + port_info[i].GetIPAddress()); // Get devices name and IP address
            }
        }

        AppData.putCustomData("BeiYangPrinter", beiYangPrinter);
        L.d(TAG, "beiyang printers : " + beiYangPrinter);
        return beiYangPrinter;
    }

    private void isAllPrintedCheck(){
        if(isIpContentMapEmpty()){
            L.d(TAG,"All content in this order are printed, to comfirm app with OK.");
            OrderIdentifierFragment.comfirmPrintOK();
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
        try {
            Date d = new Date(Long.valueOf(startTime));
            startTime = df.format(d);
        }catch(Exception e){
            startTime = "";
        }

        try{
            Date d = new Date(Long.valueOf(endTime));
            endTime = df.format(d);
        }catch(Exception e){
            Date d = new Date();
            endTime = df.format(d);
        }

        //determin the width of paper.
        String font = AppData.getCustomData(curPrintIp + "font");
        if(StringUtils.isBlank(font)) {
            font = AppData.getCustomData("font");
        }
        if(!StringUtils.isBlank(font)){
            String w = AppData.getCustomData(curPrintIp + "width");
            if(StringUtils.isBlank(w)) {
                w = AppData.getCustomData("width");
            }
            try {
                width = Integer.valueOf(w);
            }catch(Exception e){

            }
        }
        String spaceStr = generateString((width - startTime.length())/2, " ");

        StringBuilder content = new StringBuilder("\n");
        content.append(generateString((width - 6)/2, " "));
        content.append("REPORT");
        content.append("\n\n\n");
        content.append(startTime).append(" to");
        content.append("\n");
        content.append(endTime);
        content.append("\n");

        String sep_str1 = AppData.getCustomData("sep_str1");
        if(sep_str1 == null || sep_str1.length() == 0){
            sep_str1 = SEP_STR1;
        }
        content.append(generateString(width, sep_str1)).append("\n\n");
        Double total = Double.valueOf(0);
        int item = 0;
        for(SaleRecord saleRecord:saleRecords){
            String name = saleRecord.getMname();
            content.append(name);

            int lengthOfName = getLengthOfString(name);
            StringBuilder content2 = new StringBuilder();
            if(lengthOfName < 11){
                content2.append(generateString(12 - lengthOfName, " "));//x appear at the position of 13
            }else{
                content2.append(" ");
            }

            String number = String.valueOf(saleRecord.getNumber().intValue());
            content2.append("x");
            content2.append(number);
            content.append(content2);

            String price = String.format("%.2f", saleRecord.getPrice());//String.valueOf(((int)(saleRecord.getPrice() * 100))/100.0);

            int spaceLeft = width - (lengthOfName + content2.length() + price.length() + 1);
            if(spaceLeft < 2){
                content.append(" ");
            }else{
                content.append(generateString(spaceLeft, " "));
            }

            content.append("=");
            content.append(price);
            content.append("\n");

            item += Integer.valueOf(number);
            total += Double.valueOf(saleRecord.getPrice());
        }
        content.append(generateString(width, SEP_STR2)).append("\n");
        content.append(item);
        content.append(" ITEMS");

        String totalStr = String.format("%.2f", total);
        String space = generateString(width - 7 - String.valueOf(item).length() - totalStr.length(), " ");
        content.append(space);

        content.append("=");
        content.append(totalStr);
        //content.append(generateSpaceString(5)).append("* ").append(str.getName()).append(" *\n");
        content.append("\n\n\n\n\n");
        return content.toString();
    }

    private String formatContentForPrint(List<SelectionDetail> list){
        L.d(TAG,"formatContentForPrint");
        String font = AppData.getCustomData(curPrintIp + "font");
        if(StringUtils.isBlank(font)) {
            font = AppData.getCustomData("font");
        }
        if(!StringUtils.isBlank(font)){
            String w = AppData.getCustomData(curPrintIp + "width");
            if(StringUtils.isBlank(w)) {
                w = AppData.getCustomData("width");
            }
            try {
                width = Integer.valueOf(w);
            }catch(Exception e){

            }
        }
        StringBuilder content = new StringBuilder("\n\n");
        DateFormat df = new SimpleDateFormat("HH:mm");
        String tableName = CustomerSelection.getInstance().getTableNumber();
        String dateStr = df.format(new Date());
        String spaceStr = generateString(width - (2 + CustomerSelection.getInstance().getTableNumber().length() + dateStr.length()), " ");

        if(width < 20){
            content.append("\n\n");
        }

        content.append("(").append(tableName).append(")").append(spaceStr).append(dateStr).append("\n");

        String sep_str1 = AppData.getCustomData("sep_str1");
        if(sep_str1 == null || sep_str1.length() == 0){
            sep_str1 = SEP_STR1;
        }
        String sep_str2 = AppData.getCustomData("sep_str2");
        if(sep_str2 == null || sep_str2.length() == 0){
            sep_str2 = SEP_STR2;
        }

        content.append(generateString(width, sep_str1)).append("\n\n");

        for(SelectionDetail dd:list){
            StringBuilder sb = new StringBuilder();
            sb.append(dd.getDish().getID());
            sb.append(generateString(5 - dd.getDish().getID().length(), " "));
            sb.append(dd.getDish().getMname());
            if(dd.getDishNum() > 1){
                String space = " ";
                int occupiedLength = getLengthOfString(sb.toString());
                sb.append(generateString(width - occupiedLength - (dd.getDishNum() < 10 ? 2 : 3), " "));
                sb.append("X").append(Integer.toString(dd.getDishNum()));
            }
            content.append(sb);
            content.append("\n");
            if(dd.getMarkList() != null) {
                for (Mark str : dd.getMarkList()) {
                    content.append(generateString(5, " ")).append("* ").append(str.getName()).append(" *\n");
                }
            }
            content.append(generateString(width, sep_str2)).append("\n");
        }
        return content.substring(0, content.length() - (width + 1));
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

    private String generateString(int l, String character){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0;i<l;i++){
            sb.append(character);
        }
        return sb.toString();
    }

    private boolean isIpContentMapEmpty(){
        for(Map.Entry entry : ipContentMap.entrySet()){
            List<String> listTypeValue = (List<String>)entry.getValue();
            if(listTypeValue.size() > 0){
                return false;
            }
        }
        return true;
    }

}
