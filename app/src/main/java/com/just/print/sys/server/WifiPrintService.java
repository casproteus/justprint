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
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.Printer;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.ui.fragment.LoginFragment;
import com.just.print.ui.fragment.OrderCategoryFragment;
import com.just.print.ui.fragment.OrderIdentifierFragment;
import com.just.print.util.AppUtils;
import com.just.print.util.Command;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
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

import static com.just.print.util.ToastUtil.showToast;

public class WifiPrintService implements Runnable{
    private static final String REFUND_PREFIX = "<<<<";
    private final String TAG = "WifiPrintService";
    String serverip = null;
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

    public static POSSDK posSDK = null;
    //private POSSDK posSDK_state = null;
    private POSInterfaceAPI posInterfaceAPI = new POSWIFIAPI();
    //private POSInterfaceAPI posInterfaceAPI_state = new POSWIFIAPI();

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
                    contentReadyForPrintFlag = false;//in case this happen when trying to generate connection with printer.
                    printerConnectedFlag = false;
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    L.e(TAG,"Connection Error! With ip:" + WifiPrintService.curPrintIp, null);
                    showToast("Printer:" + curPrintIp + " connection error!");

                    AppUtils.sleep(1000);

                    contentReadyForPrintFlag = false;
                    printerConnectedFlag = false;

                    break;
                case WifiCommunication.SEND_FAILED:
                    L.e(TAG, "ERROR! When sending msg to: " + WifiPrintService.curPrintIp, null);
                    contentReadyForPrintFlag = false;
                    printerConnectedFlag = false;
                    //发送失败对策暂无
                    break;
                case WifiCommunication.WFPRINTER_REVMSG:
                    L.d(TAG, "got message from server, msg =: " + msg);
                    showToast("msg:" + msg);
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
        serverip = AppData.getCustomData(AppData.serverip);
        executorService.execute(this);
        L.d(TAG,"Create Service Successful");
    }

    public void reInitPrintRelatedMaps(){
        ipPrinterMap = new HashMap<String,Printer>();
        ipContentMap = new HashMap<String,List<String>>();
        ipSelectionsMap = new HashMap<String,List<SelectionDetail>>();

        List<Printer> printerList = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getPrinterDao());//读取打印机位置
        for(Printer printer:printerList){
            String ip = printer.getIp();
            ipPrinterMap.put(ip,printer);
            ipContentMap.put(ip,new ArrayList<String>());
            ipSelectionsMap.put(ip,new ArrayList<SelectionDetail>());
        }
    }

    //if the ipContnetMap(the final map which contains the content to be printed) not empty, will return error.
    //other wise will send to serer and report.(no matter the content will be printed succesfully or not, because if not printed by accident, they will manully write kitchen a bill.
    public String exePrintCommand(boolean isCancel){
        L.d(TAG,"start to translate selection into ipContent for printing.");
        String ip = getIPofNonEmptyIpContentMapEntry();
        if(ip != null){
            L.d(TAG,"ipContent not empty, means last print job not finished yet! returning not success flag.");
            return ip;                     //未打印完毕
        }
        if(checkErrorPrinterList()){
            showToast("PRINTING...");
        }
        popKitchenBillIdx();

        Double priceOfBill = saveSaleRecToDB(isCancel);                              //save to db

        if(!StringUtils.isBlank(serverip)){                     //send to server
            sendToServer(serverip);
        }

        if (manageDishesIntoMapAndWaitingForPrint(isCancel, priceOfBill)) { //send to printer
            return SUCCESS;
        }else {
            return ERROR;
        }
    }

    private boolean manageDishesIntoMapAndWaitingForPrint(boolean isCancel, Double priceOfBill) {
        //1、遍历每个选中的菜，并分别遍历加在其上的打印机。并在ipSelectionsMap上对应IP后面增加菜品
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            List<M2M_MenuPrint> printerList = selectionDetail.getDish().getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m: printerList) {
                Printer printer = m2m.getPrint();
                if(printer == null) {                   //should never happen, jist in case someone changed db.
//                    ToastUtil.showToast("Printer not exist anymore. please check" + m2m.toString());
//                    return false;
                    continue;       //we should allow user to sell dish need no print out, like cocacola~
                }
                String ip = printer.getIp();
                L.d(TAG,"Adding a dish to ipSelectionsMap, ip:" + ip);
                ipSelectionsMap.get(ip).add(selectionDetail);
            }
        }

        //2、遍历ipSelectionsMap，如对应打印机type为0, 则对其后的value(dishes)按照类别进行排序
        L.d(TAG,"checking how many dishes under each printer...");
        for(Map.Entry entry: ipSelectionsMap.entrySet()){
            String key = (String)entry.getKey();    //the key is printer ip
            List<SelectionDetail> dishList = (List<SelectionDetail>) entry.getValue(); //get all dishes to be printed in this printer.

            L.d(TAG,"ip:" + key + ", list size:" + dishList.size());
            if(ipPrinterMap.get(key).getType() != 1 && dishList.size() > 0){
                //order the dishes in this this printer.
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
        //add a kitchenBill on top, so when there's issue in printer, user can found a bill was miss printed.
        String kitchenBillIdx = AppData.curBillIdx;

        String mobileIdx = LoginFragment.getUserName();     //used for knowing printed from which mobile.
        if(!StringUtils.isBlank(mobileIdx)){                            //@TODO: should be improved by using pos to print, then no need this mobile Mark anymore.
            kitchenBillIdx = mobileIdx + kitchenBillIdx;
        }

        for(Map.Entry entry: ipSelectionsMap.entrySet()){
            String printerIP = (String)entry.getKey();
            List<SelectionDetail> dishList = (List<SelectionDetail>) entry.getValue();

            if(dishList.size() > 0){

                if(ipSelectionsMap.get(printerIP) != dishList){
                    L.d("ERROR!", "the dishList are different from ipSelectionsMap.get(printerIP)!!!!");
                }
                Printer printer = ipPrinterMap.get(printerIP);
                if(printer.getFirstPrint() == 1){  //全单封装---following each printerip(key), there will be only one formatted string
                    if(printer.getType() == 0){   //if category is set then cut by category.
                        List<SelectionDetail> tlist = new ArrayList<SelectionDetail>();
                        String currentCategory = null;
                        for(SelectionDetail selectionDetail : dishList){
                            Category c1 = Applic.app.getDaoMaster().newSession().getCategoryDao().load(selectionDetail.getDish().getCid());
                            if(currentCategory == null){    //if not set yet, then set the first value.
                                currentCategory = c1.getCname();
                                tlist.add(selectionDetail);
                            } else if(currentCategory.equals(c1.getCname())){    //if same with previous, then add into the list.
                                tlist.add(selectionDetail);
                            } else {                    //if not same, then add current list into ipContent map, and start a new list.
                                ipContentMap.get(printerIP).add(formatContentForPrint(tlist, kitchenBillIdx, isCancel, priceOfBill) + "\n\n");
                                currentCategory = c1.getCname();
                                tlist = new ArrayList<SelectionDetail>();
                                tlist.add(selectionDetail);
                            }
                        }
                        //put the last list into ipContent map.
                        ipContentMap.get(printerIP).add(formatContentForPrint(tlist, kitchenBillIdx, isCancel, priceOfBill) + "\n\n");
                    }else{
                        ipContentMap.get(printerIP).add(formatContentForPrint(dishList, kitchenBillIdx, isCancel, priceOfBill) + "\n\n\n\n\n");
                    }
                }else{                                          //分单封装---following each key(printer ip), there will be a list of formatted string.
                    for(SelectionDetail selectionDetail : dishList){
                        List<SelectionDetail> tlist = new ArrayList<SelectionDetail>();     //use list to fullfill the parameter format of the format method,
                        tlist.add(selectionDetail);                                         //only one dish will be added into this list.
                        ipContentMap.get(printerIP).add(formatContentForPrint(tlist, kitchenBillIdx, isCancel, priceOfBill) + "\n\n");
                    }
                }
            }
            //clear the ipSelectionsMap immediately
            ipSelectionsMap.get(printerIP).clear();
        }

        L.d(TAG, "Order is translated into ipContentMap map and ready for print.");
        return true;
    }

    private void popKitchenBillIdx() {
        AppData.curBillIdx = AppData.getCustomData(AppData.kitchenBillIdx);  //and it can also be used to know which order come in first.
        if(StringUtils.isBlank(AppData.curBillIdx)){
            AppData.curBillIdx = "1";
        }
        AppData.putCustomData(AppData.kitchenBillIdx, String.valueOf(Integer.valueOf(AppData.curBillIdx) + 1));   //update thee kbi into higer value.
    }

    private static Double saveSaleRecToDB(boolean isCancel) {
        Double priceFR = 0.0;
        //save the menu into database.
        SaleRecordDao saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            int number = selectionDetail.getDishNum();
            Menu menu = selectionDetail.getDish();
            String name = menu.getMname();
            Double price = menu.getPrice() * number;

            //if set as conbine mark price into dish price.
            if("true".equals(AppData.getCustomData(AppData.conbineMarkPrice))) {
                List<Mark> marks = selectionDetail.getMarkList();
                if (marks != null) {
                    for (Mark mark : marks) {
                        price += ((float) mark.getVersion()) / 100.0 * mark.getQt();
                    }
                }
            }

            if(isCancel){
                price *= -1;
            }

            SaleRecord saleRecord = new SaleRecord();
            String mName = "";
            if(AppData.showID) {
                mName = menu.getID() + " ";
            }
            if(AppData.showName) {
                mName = mName + name;             //dish name
            }

            saleRecord.setMname(name.trim());
            saleRecord.setNumber(Double.valueOf(number));
            saleRecord.setPrice(price);
            saleRecordDao.insertOrReplace(saleRecord);

            priceFR += price;

            if (!"true".equals(AppData.getCustomData("combineMarkPrice"))) {  //by defalut mark price is not added into salesrecord, so, generate saleRecord for the marks.
                List<Mark> marks = selectionDetail.getMarkList();
                if (marks != null) {
                    for (Mark mark : marks) {
                        Double markPrice = ((float) mark.getVersion()) / 100.0 * mark.getQt();
                        if(markPrice >= 0.01) {
                            SaleRecord saleRecordForMark = new SaleRecord();
                            saleRecordForMark.setMname(mark.getName());
                            saleRecordForMark.setNumber(Double.valueOf(mark.getQt()));
                            if(isCancel){
                                markPrice *= -1;
                            }
                            saleRecordForMark.setPrice(markPrice);
                            saleRecordDao.insertOrReplace(saleRecordForMark);

                            priceFR += markPrice;
                        }
                    }
                }
            }
        }
        return priceFR;
    }

    //The start time and end time are long format, need to be translate for print.
    public String exePrintReportCommand(List<SaleRecord> saleRecords, String endTime){
        if(checkErrorPrinterList()){
            showToast("PRINTING...");
        }
        if(saleRecords == null || saleRecords.size() == 0) {
            showToast("No report to print! The sales record has been cleaned!");
            return null;
        }
        L.d("ConfigPrintReportFragment","exePrintCommand");
        List<Printer> printers = Applic.app.getDaoMaster().newSession().getPrinterDao().loadAll();
        String printerIP = AppData.getCustomData(AppData.reportPrinter);
        if(printerIP == null || printerIP.length() < 8 || printerIP.indexOf(".") < 1 || printerIP.indexOf(".") > 3) {
            printerIP = printers.get(0).getIp();
        }
//        if(categorizedRecs.containsKey(category)){
//            categorizedRecs.get(category).add(saleRecord);
//        }else{
//            List<SaleRecord> list = new ArrayList<SaleRecord>();
//            list.add(saleRecord);
//            categorizedRecs.put(category, list);
//        }
        HashMap<String, SaleRecord> map = new HashMap<String, SaleRecord>();
        //combine the records
        for(SaleRecord saleRecord : saleRecords){

            String name = saleRecord.getMname();
            if(saleRecord.getPrice() < 0){
                name = REFUND_PREFIX + name;
            }

            SaleRecord exist = map.get(name);
            if(exist == null){
                map.put(name, saleRecord);
            }else{
                exist.setNumber(exist.getNumber() + saleRecord.getNumber());
                exist.setPrice(exist.getPrice() + saleRecord.getPrice());
            }
        }
        List<SaleRecord> combinedSaleRecords = new ArrayList<>();
        for(Map.Entry<String, SaleRecord> entry :map.entrySet()){
            combinedSaleRecords.add(entry.getValue());
        }

        String startTime = AppData.getCustomData(AppData.reportStartDate);
        if (startTime == null || startTime.length() < 1) {
            startTime = AppData.getCustomData(AppData.lastsuccess);
        }
        String contentFR = formatContentForPrintReport(combinedSaleRecords, startTime, endTime);
        if("true".equals(AppData.getCustomData(AppData.hideCancelItem))) {
            int p = contentFR.indexOf(" ITEMS CANCEL:  ");
            ipContentMap.get(printerIP).add(contentFR.substring(0, p) + "\n\n\n");
        }else {
            ipContentMap.get(printerIP).add(contentFR);
        }

        showToast("PRINTING REPORT ON " + printerIP);
        return contentFR;
    }

    private boolean checkErrorPrinterList() {
        StringBuilder ipStr = new StringBuilder();
        for(String ip : errorPrinterList){
            ipStr.append(",");
            ipStr.append(ip);
        }
        if(ipStr.length() > 1) {
            showToast("PRINTER ERROR, restart printer " + ipStr.substring(1) + ", and restart this app");
            return false;
        }
        return true;
    }

    private List<String> errorPrinterList = new ArrayList<String>();
    public void addProblematicPrinter(String ip){
        if(!errorPrinterList.contains(ip)) {
            errorPrinterList.add(ip);
        }
    }

    public void run(){

        beiYangPrinters = prepareBeiYangPrinterStr();

        int timeCounter = 0;
        while (true) {
            //check if this round is a good time to reset a curPrintIp and load new contnet to print.(two flags are both false)
            if (isReadyToInitNewPrintJob()) {
                //stop at the first non-empty entry, (non-empty means the values hidden in this printerIp is non-empty.)
                //and initSocket to the printer key pointing to.
                for (Map.Entry entry : ipContentMap.entrySet()) {

                    List<String> contentList = (List<String>) entry.getValue();
                    String printerIP = (String) entry.getKey();

                    if (contentList.size() > 0 && !errorPrinterList.contains(printerIP)) {
                        curPrintIp = printerIP;
                        L.d(TAG, "ip changed to:" + curPrintIp + "the size of categorizedContent to print is:" + contentList.size());
                        contentReadyForPrintFlag = true; //mark that we have found something to print, don't come into here and do init socket any more.
                        timeCounter = 0;
                        L.d(TAG, "contentReadyForPrintFlag setted up! now waiting for initSocket to set up the printerConnectedFlag");
                        connectToThePrinter(curPrintIp); //if success, an other flag (printerConnectedFlag) will be set up
                        break;  //stop for getting categorizedContent for other printers, stop here, when one printer finished, open connection to an other printer and print again.
                    }
                }

            } else if (isReadyToPrint()) {//check if this round is a good time to do actual print work? (two flags are both true)
                timeCounter = 0;
                L.d(TAG, "Flags both set, checking the categorizedContent fllowing current ip:" + curPrintIp);

                if (ipContentMap != null && ipContentMap.get(curPrintIp) != null) {

                    List<String> contentList = ipContentMap.get(curPrintIp);
                    L.d(TAG, "out printing... categorizedContent list size is:" + contentList.size());
                    String note = ipPrinterMap.get(curPrintIp).getNote();
                    try {
                        int loopTime = Integer.valueOf(note);
                        for (int i = 0; i < loopTime; i++) {
                            printContents(contentList);
                        }
                    } catch (Exception e) {
                        //note is not a number then do not loop.
                        printContents(contentList);
                    } finally {
                        //when all categorizedContent of a printer has printed, it's the right time to close conenction.
                        if (isBeiYangPrinter(curPrintIp)) {
                            closeConenctionToBeiYangPrinter();
                        } else {
                            wifiCommunication.close();
                        }
                    }

                    //reset status and get ready for a new print job( a print job = connecting to a printer + print categorizedContent + reset)
                    ipContentMap.get(curPrintIp).clear();

                    isAllPrintedCheck();
                    L.d(TAG, "Print complete (ipcontent cleaned, flag set to false, connection closed!) for ip:" + curPrintIp);
                } else {
                    L.e(TAG, "Unexpected empty Content found when printing to printer: :" + curPrintIp, null);
                    showToast("Unexpected empty Content found! when printing to printer: " + curPrintIp);
                }
            } else { //if there's onely 1 flag are false, that flag must be printerConnectedFlag.
                timeCounter++;
                if (timeCounter == 8) {
                    timeCounter = 0;
                    L.d(TAG, "printerConnectedFlag:" + printerConnectedFlag);
                    showToast("PRINTER ERROR, try restart printer " + curPrintIp + ", and restart this app");
                }
            }

            //did any work or didn't do any work, each round should rest for at least 50.
            int time = 50;
            String waitTime = AppData.getCustomData(AppData.waitTime);
            if (waitTime != null && waitTime.trim().length() > 0) {
                try {
                    time = Integer.valueOf(waitTime);
                    if (time == 0) {
                        time = 50;
                    }
                } catch (Exception e) {
                    L.e("WifiPrintService", " unexpected wait time set: " + waitTime, e);
                }
            }
            if (time > 0) {
                AppUtils.sleep(time);
            }
        }
    }

    private void sendToServer(String serverip) {
        DataSyncService instance = new DataSyncService();
        instance.serverip = serverip;
        instance.tableName = CustomerSelection.getInstance().getTableName();
        if(StringUtils.isBlank(instance.tableName)){
            instance.tableName = AppData.curBillIdx; //shouldn't use AppData.getCust... which is already increased.
        }
        instance.lastSelection = new ArrayList<SelectionDetail>();
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            instance.lastSelection.add(selectionDetail);
        }

        instance.start();
    }

    private boolean isReadyToInitNewPrintJob(){
        return contentReadyForPrintFlag == false && printerConnectedFlag == false;
    }

    private boolean isBeiYangPrinter(String printerIP){
        return beiYangPrinters.contains(printerIP);
    }

    private boolean isReadyToPrint(){
        return contentReadyForPrintFlag == true && printerConnectedFlag == true;
    }

    private void connectToThePrinter(String printerIP){

        if(isBeiYangPrinter(printerIP)){
            connectToBeiYangPrinter(printerIP);
        }else {
            wifiCommunication.initSocket(printerIP, POSPORT);   //this should result in the "printerConnectedFlag" become true .
        }
    }

    private void printContents(List<String> contents){
        //get out customized font
        String font = AppData.getCustomData(curPrintIp + AppData.font);
        if (StringUtils.isBlank(font)) {
            font = AppData.getCustomData(AppData.font);
        }
        if(isPrintingReport(contents)){
            font = AppData.getCustomData(AppData.reportFont);
            if(font == null || font.length() < 6) {
                font = "27,33,0";
            }
        }
        for (String content : contents) {    //might print several times, if the printer is setted as "DanDa"

            if (content.length() <= 0) {
                L.e("Empty categorizedContent found for print!", curPrintIp, null);
                continue;
            }

            if (isBeiYangPrinter(curPrintIp)) {
                doBeiYangPrint(font, content);  //check if it's nbna printer, check printer status
            } else {
                doZiJiangPrint(font, content);
            }
        }
    }

    private boolean isPrintingReport(List<String> contents) {
        if(contents == null || contents.size() == 0)
            return false;
        int p = contents.get(0).indexOf(getReportFirstLineContent().toString());
        return p == 0;
    }

    private StringBuilder getReportFirstLineContent() {
        String mobileMark = LoginFragment.getUserName();
        StringBuilder content = new StringBuilder(mobileMark == null ? "" : mobileMark);
        String idx = AppData.getCustomData(AppData.reportIdx);
        return content.append(idx == null || idx.length() == 0 ? "1" : idx);
    }

    private void connectToBeiYangPrinter(String printerIP){
        return_code = posInterfaceAPI.OpenDevice(printerIP, POSPORT);
        if(return_code != POS_SUCCESS){
            L.e(TAG, "Open PosPort Failed", null);
            showToast("Open PosPort Failed. Check Printer!");
            return;
        }else{
            if(posSDK != null){
                posSDK = null;
            }
            posSDK = new POSSDK(posInterfaceAPI);
        }

        /**return_code = posInterfaceAPI_state.OpenDevice(printerIP, STATEPORT);
         if(return_code != POS_SUCCESS){
         L.e(TAG, "STATEPORT Open Failed!", null);
         ToastUtil.showToast("State Port Open Failed! Check Printer!");
         }else{
         if(posSDK_state != null){
         posSDK_state = null;
         }
         posSDK_state = new POSSDK(posInterfaceAPI_state);
         }

         if (return_code != POS_SUCCESS) {
         ToastUtil.showToast("Failed to connect device, please try again.");
         }**/
        printerConnectedFlag = true;
    }

    private void closeConenctionToBeiYangPrinter(){
        return_code = posInterfaceAPI.CloseDevice();
        if (return_code != POS_SUCCESS) {
            showToast("Failed to close printer.");
            L.e("Failed to close printer.", curPrintIp, null);
        }
        /**
         return_code = posInterfaceAPI_state.CloseDevice();
         if(return_code != POS_SUCCESS){
         ToastUtil.showToast("Failed to close printer.");
         L.e("Failed to close printer status port.", curPrintIp, null);
         }**/

        resetFlags();
    }

    public void resetFlags() {
        printerConnectedFlag = false;
        contentReadyForPrintFlag = false;
    }

    private void doBeiYangPrint(String font, String content){
        //get out customized line_height
        String line_height = AppData.getCustomData(curPrintIp + "line_height");
        if (StringUtils.isBlank(line_height)) {
            line_height = AppData.getCustomData("line_height");
        }

        String font_w = AppData.getCustomData(curPrintIp + "font_w");
        if (StringUtils.isBlank(font_w)) {
            font_w = AppData.getCustomData("font_w");
        }
        if (StringUtils.isBlank(font_w)) {
            font_w = font;
        }

        String font_h = AppData.getCustomData(curPrintIp + "font_h");
        if (StringUtils.isBlank(font_h)) {
            font_h = AppData.getCustomData("font_h");
        }
        if (StringUtils.isBlank(font_h)) {
            font_h = font;
        }


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
        try {
            LineHeight = Integer.valueOf(line_height);
        } catch (Exception e) {
        }

        int HorizontalTimes = 6;//Get HorizontalTimes
        int VerticalTimes = 6;//Get VerticalTimes
        try {
            HorizontalTimes = Integer.valueOf(font_w);
        } catch (Exception e) {
        }
        try {
            VerticalTimes = Integer.valueOf(font_h);
        } catch (Exception e) {
        }

        return_code = testprint.TestPrintText(posSDK, printMode, content, content.length(), FontType, FontStyle,
                Alignment, HorStartingPosition, VerStartingPosition, LineHeight, HorizontalTimes, VerticalTimes);
        if (return_code != POS_SUCCESS) {
            showToast("Failed to print Text.");
            L.e("Failed to print Text.", "return code is :" + return_code, null);

            final int QueryStatusSize = 4;
            byte[] StatusBuffer = new byte[QueryStatusSize];
            return_code =
                    posSDK.systemQueryStatus(StatusBuffer, QueryStatusSize, 1);
            if (return_code == POS_SUCCESS) {
                StringBuilder sb = new StringBuilder(curPrintIp);
                for (int i = 0; i < QueryStatusSize; i++) {
                    sb.append("_");
                    sb.append(StatusBuffer[i]);
                }
                L.e("Printer status:", sb.toString(), null);
            }else{
                showToast("Failed to get printer's status.");
                L.e("Failed to print Text.", "return code is :" + return_code, null);
            }
        }
        return_code = posSDK.systemCutPaper(66, 10);
    }

    private void doZiJiangPrint(String font, String content){
        if (!"silent".equals(AppData.getCustomData(AppData.mode))) {
            wifiCommunication.sndByte(Command.BEEP);
        }

        if (StringUtils.isBlank(font)) {
            wifiCommunication.sndByte(new byte[] {0x1b, 0x21, 0x33 });// commentted out because variable id not chaged, while the content is modified to 27,33,0 (should be 27,33,51)Command.GS_ExclamationMark);
        } else {
            //default: "27, 33, 48" because it works for both thermal and non-thermal
            String[] pieces = font.split(",");
            if (pieces.length != 3) {
                L.e("Invalid font format found!", font,null);
                wifiCommunication.sndByte(Command.GS_ExclamationMark);
            } else {
                for (int i = 0; i < 3; i++) {
                    Command.GS_ExclamationMark[i] = Integer.valueOf(pieces[i].trim()).byteValue();
                }
                wifiCommunication.sndByte(Command.GS_ExclamationMark);
            }
        }

        //code can be customzed
        String tCode = AppData.getCustomData(AppData.code);
        if (tCode != null && tCode.length() > 2) {
            code = tCode;
        }
        wifiCommunication.sendMsg(content, code);

        //cut the paper.
        wifiCommunication.sndByte(Command.GS_V_m_n);

    }

    private String prepareBeiYangPrinterStr(){
        String beiYangPrinter = AppData.getCustomData(AppData.BeiYangPrinter);
        if(beiYangPrinter != null && beiYangPrinter.length() > 7){
            return beiYangPrinter;
        }

        beiYangPrinter = "";    //@note: we don't want to return a null back to cause any null pointer.
        if( "true".equalsIgnoreCase(AppData.getCustomData(AppData.autoSearchBeiYang))) {
            for (int i = 0; i < SearchPortMAX; i++) {
                port_info[i] = new SearchPortInfo();
            }

            L.d(TAG, "Searching devices");
            int sch_prt_num = posInterfaceAPI.WIFISearchPort(port_info, SearchPortMAX);
            if (sch_prt_num <= 0) {
                L.d(TAG, "No BeiYang WIFI Printer devices found");
            } else {
                for (int i = 0; i < sch_prt_num; i++) {
                    if (beiYangPrinter.indexOf(port_info[i].GetIPAddress()) == -1)
                        beiYangPrinter.concat("," + port_info[i].GetIPAddress()); // Get devices name and IP address
                }
            }

            AppData.putCustomData(AppData.BeiYangPrinter, beiYangPrinter);
            L.d(TAG, "beiyang printers : " + beiYangPrinter);
        }
        return beiYangPrinter;
    }

    private void isAllPrintedCheck(){
        if(getIPofNonEmptyIpContentMapEntry() == null){
            L.d(TAG,"All categorizedContent in this order are printed, to comfirm app with OK.");
            OrderIdentifierFragment.confirmPrintOK();
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
        DateFormat df = new SimpleDateFormat("MM/dd HH:mm");
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
        width = 42;
        String reportWidth = AppData.getCustomData(AppData.reportWidth);
        try{
            width = Integer.valueOf(reportWidth);
        }catch(Exception e){
        }

        String spaceStr = generateString((width - startTime.length())/2, " ");

        //start the report contnt---------------------------------------------------
        StringBuilder content = getReportFirstLineContent().append("\n");

        //the second line and the third line.
        content.append(generateString((width - 6)/2, " "));
        content.append("REPORT");
        content.append("\n\n\n");
        content.append(startTime).append("--").append(endTime);
        content.append("\n");
        int lastBillNo = 0;
        try {
            lastBillNo = Integer.valueOf(AppData.getCustomData(AppData.kitchenBillIdx)) - 1;
        }catch (Exception e){
            L.e("WifiPrintService ", "Exception when getting kitchenBillIdx for report, the last bill was found to be:" + lastBillNo, e);
        }
        content.append("last bill : #").append(lastBillNo);
        content.append("\n");
        String sep_str1 = AppData.getCustomData(AppData.sep_str1);
        if(sep_str1 == null || sep_str1.length() == 0){
            sep_str1 = SEP_STR1;
        }
        content.append(generateString(width, sep_str1)).append("\n\n");

        //sales categorizedContent------------------------
        Double total = Double.valueOf(0);
        Double cancTotal = Double.valueOf(0);

        int qtTotal = 0;
        int qtCancel = 0;
        String oldCategory = null;
        Double subTotal = Double.valueOf(0);
        int subQt = 0;
        saleRecords = sortSaleRecordsByCategory(saleRecords);
        for(SaleRecord saleRecord:saleRecords){
            String name = saleRecord.getMname();
            String number = String.valueOf(saleRecord.getNumber().intValue());
            Double price = saleRecord.getPrice();
            String priceStr = String.format("%.2f", price);//String.valueOf(((int)(tmpPrice * 100))/100.0);

            //added it into the map, to make it regrouped by categorize.
            String category = getCategoryByName(name);
            if(!category.equals(oldCategory) && oldCategory != null){
                content.append(generateString(width, SEP_STR2)).append("\n");
                content.append(oldCategory);
                StringBuilder subTotalLine = new StringBuilder("Qty=").append(subQt)
                        .append(" Sub=").append(String.format("%.2f", subTotal));
                content.append(generateString(width - oldCategory.length() - subTotalLine.length(), " "))
                        .append(subTotalLine).append("\n\n");

                //reset
                subQt = Integer.valueOf(number);
                if(price < 0){
                    subQt = 0 - subQt;
                }
                subTotal = Double.valueOf(price);
            }else{
                if(price < 0){
                    subQt -= Integer.valueOf(number);
                }else {
                    subQt += Integer.valueOf(number);
                }
                subTotal += Double.valueOf(price);
            }

            oldCategory = category;

            if(price < 0){
                name = REFUND_PREFIX + name;
            }

            //adjust the length of item name.
            int lengthOfName = getLengthOfString(name);
            int maxLength = 30;
            try {
                maxLength = Integer.valueOf(AppData.getCustomData(AppData.menuNameLength));
            }catch(Exception e){
            }
            if(lengthOfName >= maxLength){
                name = trunkDishName(name, maxLength);
                lengthOfName = getLengthOfString(name);
            }
            content.append(name);

            //number
            content.append(generateString(maxLength + 1 - lengthOfName, " "));//x appear at the position of 13
            content.append("x");
            content.append(number);

            //price
            int spaceLeft = width - (content.length() + priceStr.length() + 1);
            if(spaceLeft < 2){
                content.append(" ");
            }else{
                content.append(generateString(spaceLeft, " "));
            }
            content.append("=");
            if(price > 0){
                content.append(" ");
            }
            content.append(priceStr);
            content.append("\n");

            //count total qt and total price. which will be displayed at the end of the report.
            if(price < 0){
                qtCancel += Integer.valueOf(number);
                cancTotal += price;
            }else {
                qtTotal += Integer.valueOf(number);
                total += Double.valueOf(price);
            }
        }
        content.append(generateString(width, SEP_STR2)).append("\n");
        content.append(oldCategory);
        StringBuilder subTotalLine = new StringBuilder("Qty=").append(subQt)
                .append(" Sub=").append(String.format("%.2f", subTotal));
        content.append(generateString(width - oldCategory.length() - subTotalLine.length(), " "))
                .append(subTotalLine).append("\n\n");

        content.append(generateString(width, SEP_STR1)).append("\n");
        content.append(" ITEMS SENT:    ").append(qtTotal).append("\n");
        content.append(" TOTAL SENT:    $").append(String.format("%.2f", total)).append("\n\n");
        content.append(" ITEMS CANCEL:  ").append(qtCancel).append("\n");
        content.append(" TOTAL CANCEL:  $").append(String.format("%.2f", cancTotal)).append("\n");
        content.append(generateString(width, SEP_STR1)).append("\n");
        content.append(" NET:           $").append(String.format("%.2f", total + cancTotal));
            //categorizedContent.append(generateSpaceString(5)).append("* ").append(str.getName()).append(" *\n");
        content.append("\n\n\n\n\n");
        return content.toString();
    }

    private String trunkDishName(String content, int maxLength) {
        int length = content.length();
        int realWidth = 0;
        for(int i = 0; i < length; i++) {
            realWidth++;
            char c = content.charAt(i);
            if(c >=19968 && c <= 171941) {
                realWidth++;
                if(realWidth == maxLength){
                    return content.substring(0, i - 3 ) + "...";
                }
            }
        }

        return content;
    }

    private List<SaleRecord> sortSaleRecordsByCategory(List<SaleRecord> saleRecords) {
        Map<String, List<SaleRecord>> map = new HashMap<String, List<SaleRecord>>();
        for (SaleRecord saleRecord : saleRecords) {
            String category = getCategoryByName(saleRecord.getMname());
            if(map.containsKey(category)){
                map.get(category).add(saleRecord);
            }else{
                ArrayList<SaleRecord> list = new ArrayList<SaleRecord>();
                list.add(saleRecord);
                map.put(category, list);
            }
        }
        List<SaleRecord> newOrderRecords = new ArrayList<>();
        for (Map.Entry<String, List<SaleRecord>> entry: map.entrySet()) {
            List<SaleRecord> records = entry.getValue();
            Collections.sort(records, new Comparator<SaleRecord>() {
                @Override
                public int compare(SaleRecord rec1, SaleRecord rec2) {
                    return rec1.getId().compareTo(rec2.getId());
                }
            });
            newOrderRecords.addAll(records);
        }
        return newOrderRecords;
    }

    private String getCategoryByName(String name) {
        for(Map.Entry<Category, List<Menu>> entry: OrderCategoryFragment.getCategorizedContent().entrySet()){
            List<Menu> list = entry.getValue();
            for (Menu menu: list) {
                if(name.equals(menu.getMname()) || name.equals(menu.getID() + " " + menu.getMname())){
                    return entry.getKey().getCname();
                }
            }
        }
        return "";
    }

    private void determinTheWidth() {
        String font = AppData.getCustomData(curPrintIp + AppData.font);
        if(StringUtils.isBlank(font)) {
            font = AppData.getCustomData(AppData.font);
        }
        if(!StringUtils.isBlank(font)){
            String w = AppData.getCustomData(curPrintIp + AppData.width);
            if(StringUtils.isBlank(w)) {
                w = AppData.getCustomData(AppData.width);
            }
            try {
                width = Integer.valueOf(w);
            }catch(Exception e){

            }
        }else{
            width = 24;
        }
    }

    private String formatContentForPrint(List<SelectionDetail> list, String kitchenBillIdx, boolean isCancel, Double priceOfBill) {
        L.d(TAG, "formatContentForPrint");

        determinTheWidth();

        //leave 2 space, incase the printer prints extra characters at the left corner.
        String SEPRATOR = isCancel ? "<" : " ";

        StringBuilder content = new StringBuilder();
        //tiltle---could be used for kithch name, empty lines.......
        String title = AppData.getCustomData(AppData.kitchentitle);
        if (title.length() > 0) {
            if (title.endsWith("lines")) {
                title = title.substring(0, title.length() - 5).trim();
                try {
                    int qt = Integer.valueOf(title);
                    for (int i = 0; i < qt; i++) {
                        content.append("\n");
                    }
                } catch (Exception e) {
                    L.e("WifiPrint", "when seeting title", e);
                }
            } else {
                content.append(title);
            }
        }
        //if it's cancel, then add Cancel.........................
        if (isCancel) {
            content.append("       *** (取消CANCEL) ***\n");
        }
        if (width < 20) {
            content.append("\n\n");
        }
        //table name and bill index and  print time.......................
        DateFormat df = new SimpleDateFormat("HH:mm");
        String tableName = CustomerSelection.getInstance().getTableName();
        String dateStr = df.format(new Date());

        if("1".equals(AppData.getCustomData(AppData.format_style))){
            String billIdx_Position = AppData.getCustomData(AppData.title_position);
            if ("left".equals(billIdx_Position)) {
                //add no space.
            } else if ("right".equals(billIdx_Position)) {
                content.append(generateString(width - kitchenBillIdx.length(), " "));
            }else{  //center is the default
                content.append(generateString((width - kitchenBillIdx.length()) / 2, " "));
            }
            content.append(kitchenBillIdx).append("\n\n");
            //kitchenBillIdx and time
            if(tableName != null && tableName.length() > 0) {
                content.append("(").append(tableName).append(")");
            }
            content.append(generateString(width - tableName.length() - 2 - dateStr.length(), SEPRATOR))
                    .append(dateStr).append("\n");
        }else {
            //table name
            if(tableName != null && tableName.length() > 0) {
                String tablename_position = AppData.getCustomData(AppData.title_position);
                if ("left".equals(tablename_position)) {
                    //add nothing
                } else if ("right".equals(tablename_position)) {
                    content.append(generateString(width - tableName.length() - 2, " "));
                } else {  //center is the default
                    content.append(generateString((width - tableName.length() - 2) / 2, " "));
                }
                content.append("(").append(tableName).append(")").append("\n\n");
            }
            //kitchenBillIdx and time
            content.append(kitchenBillIdx)
                    .append(generateString(width - kitchenBillIdx.length() - dateStr.length(), SEPRATOR))
                    .append(dateStr).append("\n");
        }
        //Seperator

        String sep_str1 = AppData.getCustomData(AppData.sep_str1);
        if(sep_str1 == null || sep_str1.length() == 0){
            sep_str1 = SEP_STR1;
        }
        String sep_str2 = AppData.getCustomData(AppData.sep_str2);
        if(sep_str2 == null || sep_str2.length() == 0){
            sep_str2 = SEP_STR2;
        }

        content.append(generateString(width, sep_str1)).append("\n\n");

        //Main contents starts here.........................................................
        for(SelectionDetail dd:list){
            StringBuilder sb = new StringBuilder();
            if(AppData.showID) {
                sb.append(dd.getDish().getID());                //dish id
                sb.append(generateString(5 - dd.getDish().getID().length(), SEPRATOR));
            }
            if(AppData.showName) {
                sb.append(dd.getDish().getMname());             //dish name
            }
            if(dd.getDishNum() > 1){                        //dish number
                String space = SEPRATOR;
                int occupiedLength = getLengthOfString(sb.toString());
                sb.append(generateString(width - occupiedLength - (dd.getDishNum() < 10 ? 2 : 3), SEPRATOR));
                sb.append("X").append(Integer.toString(dd.getDishNum()));
            }
            content.append(sb);
            content.append("\n");
            if(dd.getMarkList() != null) {                  //dish marks.
                for (Mark mark : dd.getMarkList()) {
                    content.append(generateString(5, SEPRATOR)).append("* ").append(mark).append(" *\n");
                }
            }
            content.append(generateString(width, sep_str2)).append("\n");
        }
        if("true".equals(AppData.getCustomData(AppData.priceonkitchenbill))) {
            return content.append(priceOfBill).append("\n").toString();
        }else {
            return content.substring(0, content.length() - (width + 1));
        }
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

    private String getIPofNonEmptyIpContentMapEntry(){
        for(Map.Entry entry : ipContentMap.entrySet()){
            List<String> listTypeValue = (List<String>)entry.getValue();
            if(listTypeValue.size() > 0){
                if(!errorPrinterList.contains(entry.getKey())) {
                    return entry.getKey().toString();
                }
            }
        }
        return null;
    }

}
