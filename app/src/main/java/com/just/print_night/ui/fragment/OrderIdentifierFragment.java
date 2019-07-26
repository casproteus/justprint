package com.just.print_night.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.widget.ViewSwitcher;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.app.EventBus;
import com.just.print_night.db.bean.Mark;
import com.just.print_night.db.bean.Menu;
import com.just.print_night.db.bean.SaleRecord;
import com.just.print_night.db.dao.SaleRecordDao;
import com.just.print_night.db.expand.DaoExpand;
import com.just.print_night.sys.model.SelectionDetail;
import com.just.print_night.sys.server.CustomerSelection;
import com.just.print_night.sys.server.WifiPrintService;
import com.just.print_night.ui.activity.ConfigActivity;
import com.just.print_night.ui.holder.OrderMarksSelectionViewHolder;
import com.just.print_night.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print_night.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print_night.ui.holder.OrderMenuViewHolder;
import com.just.print_night.util.L;
import com.just.print_night.util.StringUtils;
import com.just.print_night.util.ToastUtil;
import com.stupid.method.adapter.IXOnItemClickListener;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class OrderIdentifierFragment extends BaseFragment implements View.OnClickListener, OnClickItemListener, EventBus.EventHandler{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";
    private static final String TAG = "OrderIdentifierFragment";

    private static OrderIdentifierFragment instance = null;
    private static boolean isCancel;
    private List<Mark> allMarks;
    private List<Mark> marksOfCurDish;
    private int curmarkitem;

    public static OrderIdentifierFragment getInstance(){
        if(instance == null){
            instance = new OrderIdentifierFragment();
        }
        return instance;
    }

    @XViewByID(R.id.viewSwitcher)
    private ViewSwitcher viewSwitcher;

    @XViewByID(R.id.odIdFrLoutMenuList)
    private ListView odIdFrLoutMenuList;

    @XViewByID(R.id.odIdInput)
    private TextView odIdInput;

    @XViewByID(R.id.odIdfrName)
    private TextView odIdfrName;

    @XViewByID(R.id.odIdLoutItemsGv)
    private GridView odIdLoutItemsGv;

    @XViewByID(R.id.odIdMarksGrid)
    private GridView odIdMarksGrid;

    @XViewByID(R.id.odIdTableTbtn)
    ToggleButton odIdTableTbtn;

    @XViewByID(R.id.allMarkList)
    ListView markAllList;

    private Menu storedMenu;

    XAdapter2<Mark> markShorCutXAdapter;
    XAdapter2<Menu> menuXAdapter;
    XAdapter2<SelectionDetail> dishesXAdapter;
    XAdapter2<String> itemXAdapter;
    XAdapter2<Mark> markAllXAdapter;

    private List<Mark> shortCutMarks;

    public static List<SelectionDetail> bkOfLastSelection;
    private static CharSequence bkOfLastTable;
    static int times = 0;
    String[] items = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "H", "S", "U", "+", "togo", "canc"};

    @XClick({R.id.odIdConfigBtn, R.id.odIdSndBtn, R.id.odIdDelBtn, R.id.odIdOkBtn})
    private void exeControlCommand(View v) {
        switch (v.getId()) {
            case R.id.odIdConfigBtn:
                startActivity(new Intent(getContext(), ConfigActivity.class));
                break;
            case R.id.odIdSndBtn:
                printCurrentSelection(false);
                break;
            case R.id.odIdDelBtn:
                if( viewSwitcher.getDisplayedChild() == 0) {
                    DelOneText();
                }else{
                    viewSwitcher.setDisplayedChild(0);
                }
                break;
            case R.id.odIdOkBtn:
                String adminPassword = AppData.getCustomData("adminPassword");
                if(adminPassword == null || adminPassword.length() < 6){
                    adminPassword = "EE11EE";
                }
                if(adminPassword.equals(odIdTableTbtn.getText().toString()) || adminPassword.equals(odIdInput.getText().toString())){
                    //print report
                    List<SaleRecord> orders = Applic.app.getDaoMaster().newSession().getSaleRecordDao().loadAll();

                    if(orders == null || orders.size() == 0){
                        showToast("No report to print! The sales record has been cleaned!");
                    }else {
                        String reportStartDate = AppData.getCustomData("reportStartDate");
                        if (reportStartDate == null || reportStartDate.length() < 1) {
                            reportStartDate = AppData.getCustomData("lastsuccess");
                        }

                        //print code:
                        String result = WifiPrintService.getInstance().exePrintReportCommand(orders, reportStartDate, String.valueOf(new Date().getTime()));
                        if ("0".equals(result)) {
                            findViewById(R.id.viewSwitcher).setVisibility(View.INVISIBLE);
                            findViewById(R.id.topButtons).setVisibility(View.INVISIBLE);
                            findViewById(R.id.alertDlg).setVisibility(View.VISIBLE);
                        }
                    }
                    odIdTableTbtn.setText("");
                    odIdInput.setText("");
                }else if( viewSwitcher.getDisplayedChild() == 0) {
                    if (odIdTableTbtn.isChecked()) {    //waiting for input table num status. always allowed!
                        odIdTableTbtn.setChecked(false);
                        CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                    } else {                            //inputting dishes status. need to check print status.

                        if (bkOfLastSelection != null) {      //categorizedContent not all send to printer yet.
                            //if last order has only one dish and it happened to be same
                            //as the one selected this time, waiter will think the roll back is not happenning.
                            //we decided to not display last order--- rollbackLastOrder();
                            ToastUtil.showToast("Printing...Please wait.");
                            return;
                        } else if (storedMenu != null) {     //last order send to printer well.
                            addDish();
                            odIdInput.setText("");
                            odIdfrName.setText("");
                            if (dishesXAdapter != null) {
                                loadOrderMenu();
                            }
                        }
                    }
                }else{
                    updateSelectionOfCurrentDish();
                }
                break;

//            case R.id.odMarkSelectionCancel:
//                viewSwitcher.setDisplayedChild(0);
//                break;
//
//            case R.id.odMarkSelectionOK:
//                updateSelectionOfCurrentDish();
//                break;
        }

    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.viewSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        Applic.app.getDaoMaster().newSession().getSaleRecordDao().deleteAll();
        AppData.putCustomData("reportStartDate", String.valueOf(new Date().getTime()));
        int reportIdx = 1;
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData("reportIdx"));
        }catch(Exception e){
            //report error.
        }
        AppData.putCustomData("reportIdx", String.valueOf(reportIdx + 1));

        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.viewSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();
    }

    private void updateSelectionOfCurrentDish() {
        for(int i = 0; i < allMarks.size(); i++){
            Mark mark = allMarks.get(i);
            boolean matched = false;
            for(int j = 0; j < marksOfCurDish.size(); j++){
                if(marksOfCurDish.get(j).getName().equals(mark.getName())){
                    if(mark.getQt() == 0){
                        marksOfCurDish.remove(j);
                    }else {
                        marksOfCurDish.get(j).setQt(mark.getQt());
                    }
                    matched = true;
                    break;
                }
            }
            if(!matched && mark.getQt() > 0){
                marksOfCurDish.add(mark.clone());
            }
        }
        viewSwitcher.setDisplayedChild(0);
        //menuXAdapter.notifyDataSetChanged();
        dishesXAdapter.get(curmarkitem).setMarkList(marksOfCurDish);

        loadOrderMenu();
    }

    private boolean printCurrentSelection(boolean isCancel) {
        List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
        if(selectionDetails == null || selectionDetails.size() == 0){
            showToast("Nothing selected!");
            return false;
        }else {
            String result = WifiPrintService.getInstance().exePrintCommand(isCancel);
            if (WifiPrintService.ERROR.equals(result)) {
                if (times < 1) {
                    showToast("Last print not done yet. Please wait.");
                    times++;
                } else if (times < 2) {
                    showToast("Last print not done yet. Press Send Again To Resend it!");
                    times++;
                } else {
                    WifiPrintService.getInstance().reInitPrintRelatedMaps();
                    if (WifiPrintService.SUCCESS.equals(WifiPrintService.getInstance().exePrintCommand(isCancel))) {
                        times = 0;
                        clearOrderMenu();
                    }
                    showToast("Order was re-send!");
                }
                return false;
            } else {
                times = 0;
                clearOrderMenu();
                return true;
            }
        }
    }

    //currently it's used by number buttons and the note tag buttons. both types are buttons in holder.
    IXOnItemClickListener itemXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            L.d(TAG, "onClickItem");
            Menu tmpMenu;
            L.d(TAG, String.valueOf(view.getId()) + String.valueOf(i));
            switch (view.getId()) {
                case R.id.buttonholder: //number buttons.
                    if (i < 10) {
                        InputText(Integer.toString((i + 1) % 10));
                    } else if (i == items.length - 5) {
                        InputText("S");
                    } else if (i == items.length - 4) {
                        InputText("U");
                    } else if (i == items.length - 3) {
                        InputText("+");
                    } else if (i == items.length - 2) {
                        odIdTableTbtn.setText("TOGO");
                        CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                    } else if (i == items.length - 1) {
                        odIdTableTbtn.setText("");
                        //added the selected dish with negative price.
                        if (printCurrentSelection(true)) {    //arranged to print---didn't met the case that previous print not finished yet.
                            isCancel = true;
                        }
                    } else if (items.length == 20 && i == 16) {
                        InputText("H");
                    } else {
                        InputText(Character.toString((char) (i % 10 + 'A')));
                    }

                    if (!odIdTableTbtn.isChecked()) {
                        tmpMenu = SearchMenuFromDB(odIdInput.getText().toString());
                        if (null != tmpMenu) {
                            odIdfrName.setText(tmpMenu.getMname());
                        } else {
                            odIdfrName.setText("");
                        }
                    }
                    break;
                case R.id.tagid:            //not tag buttons.
                    L.d(TAG, "case tag");
                    List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
                    //Mark Press
                    if (selectionDetails != null && selectionDetails.size() > 0) {
                        Mark selectedMark = markShorCutXAdapter.getItem(i);
                        L.d(TAG, String.valueOf(selectedMark.select));
                        List<Mark> marks = selectionDetails.get(selectionDetails.size() - 1).getMarkList();
                        boolean matched = false;
                        for(int m = 0; m < marks.size(); m++){
                            Mark mark = marks.get(m);
                            if(mark.getName().equals(selectedMark.getName())){
                                mark.setQt(mark.getQt() + 1);
                                matched = true;
                                break;
                            }
                        }
                        if(!matched){
                            Mark m = selectedMark.clone();
                            m.setQt(1);
                            marks.add(m);
                        }
                        loadOrderMenu();
                    }
            }

            //odIdfrName.setText(menuXAdapter.get(i).getMname());
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.order_identifier_fragment;
    }

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (EVENT_ADD_MENU.equals(eventName)) {
            loadOrderMenu();
        }
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        getEventBus().register(EVENT_ADD_MENU, this);
        new StupidReflect(this, getView()).init();
        //设置餐桌号用
        //CustomerSelection.getInstance().setTableNumber(odIdTableNumEt.getText().toString());
        storedMenu = null;
        String definedLast = AppData.getCustomData(AppData.KEY_CUST_LAST_CHAR);
        items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C"};
        List<String> ary = new ArrayList<String>();
        for(int i = 0; i < items.length; i++){
            ary.add(items[i]);
        }

        if(!StringUtils.isBlank(definedLast)) {
            char maxChar = definedLast.charAt(0);
            char lastChar = items[items.length - 1].charAt(0);
            while(lastChar < maxChar){
                lastChar++;
                ary.add(String.valueOf(lastChar));
            }
        }

        ary.add("S");
        ary.add("U");
        ary.add("+");
        ary.add("togo");
        ary.add("canc");
        items = ary.toArray(items);

        itemXAdapter = new XAdapter2<String>(getActivity(), Arrays.asList(items), OrderIdentifierItemViewHolder.class);
        itemXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdLoutItemsGv.setAdapter(itemXAdapter);

        odIdTableTbtn.setTextOn(null);
        odIdTableTbtn.setTextOff(null);
        odIdTableTbtn.setText("");
        odIdTableTbtn.setChecked(true);
        odIdTableTbtn.setOnClickListener(this);

        //initSelected dishes part.
        dishesXAdapter = new XAdapter2<SelectionDetail>(getContext(), OrderMenuViewHolder.class);
        dishesXAdapter.setClickItemListener(this);
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        odIdFrLoutMenuList.setAdapter(dishesXAdapter);

        //add Marks
        allMarks = sortMarks(DaoExpand.queryNotDeletedAll(Applic.getMarkDao()));
        //add shortcut Marks.
        initShortCutMarks();

        //init MarkSelection panel
        markAllXAdapter = new XAdapter2<Mark>(getContext(), OrderMarksSelectionViewHolder.class);
        markAllXAdapter.setClickItemListener(this);
        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
    }

    @Override
    public void onClick(View v) {
        int num;
        switch (v.getId()) {
            case R.id.odIdTableTbtn:
                L.d(TAG, "ClickTableButton");
                if (odIdTableTbtn.isChecked()) {
                    odIdTableTbtn.setText("");
                }
                break;
        }
    }

    private void InputText(String str) {
        if (odIdTableTbtn.isChecked()) {
            odIdTableTbtn.append(str);
        } else {
            odIdInput.append(str);
        }

    }

    private void DelOneText() {
        if (odIdTableTbtn.isChecked()) {
            if (odIdTableTbtn.length() > 0) {
                odIdTableTbtn.setText(odIdTableTbtn.getText().subSequence(0, odIdTableTbtn.length() - 1));
            }
        } else {
            if (odIdInput.length() > 0) {
                odIdInput.setText(odIdInput.getText().subSequence(0, odIdInput.length() - 1));
            }
        }
    }

    private Menu SearchMenuFromDB(String context) {
        List<Menu> mnlist = DaoExpand.queryFuzzyMenu(Applic.app.getDaoMaster().newSession().getMenuDao(), context);
        if (mnlist.size() >= 1) {
            Collections.sort(mnlist, new Comparator<Menu>() {
                @Override
                public int compare(Menu m1, Menu m2) {
                    return m1.getMname().compareTo(m2.getMname());
                }
            });
            storedMenu = mnlist.get(0);
            return mnlist.get(0);
        }
        if (context.length() == 0) {
            return null;
        }
        return null;
    }

    @Override
    public void onClickItem(View view, int i) {
        L.d(TAG, "onClickItem");
        switch (view.getId()) {
            case R.id.oddelDish:
                SelectionDetail a = dishesXAdapter.get(i);
                //dishesXAdapter.remove(a);
                CustomerSelection.getInstance().deleteSelectedDish(a);
                loadOrderMenu();
                break;

            case R.id.odMnLoutMarkBtn:
                curmarkitem = i;
                marksOfCurDish = dishesXAdapter.get(i).getMarkList();
                showMarksPanel();

                break;
        }
    }

    private void showMarksPanel(){   //final onChoiceMarks choiceMarks) {

        if (marksOfCurDish == null) {
            marksOfCurDish = new ArrayList<Mark>();
        }

        for(int j = 0; j < allMarks.size(); j++) {
            Mark mark = allMarks.get(j);
            boolean matched = false;
            for(int i = 0; i < marksOfCurDish.size(); i++){
                String markName = marksOfCurDish.get(i).getName();
                if(mark.getName().equals(markName)){
                    matched = true;
                    mark.setQt(marksOfCurDish.get(i).getQt());
                    break;
                }
            }

            if(!matched) {
                mark.setQt(0);
            }
        }


        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
        markAllList.setSelection(0);

        if (viewSwitcher.getDisplayedChild() == 0){
            viewSwitcher.setDisplayedChild(1);
        }
//        new AlertDialog.Builder(this.getActivity()).setMultiChoiceItems(markNames, selectedQT, new DialogInterface.OnMultiChoiceClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                if (isChecked)
//                    sa.put(Integer.toString(which), which);
//                else
//                    sa.remove(Integer.toString(which));
//            }
//        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                List<Mark> result = new ArrayList<Mark>();
//                for (Integer i : sa.values()) {
//                    result.add(allMarks.get(i));
//                }
//                choiceMarks.onChoiceMarks(result);
//
//            }
//        }).setNegativeButton("Cancel", null).show();
    }

    private void addDish() {
        SelectionDetail selectedDish = new SelectionDetail();
        selectedDish.setDish(storedMenu);
        selectedDish.setMarkList(new ArrayList<Mark>());
        L.d(TAG, shortCutMarks.toString());
        selectedDish.setDishNum(1);
        CustomerSelection.getInstance().addSelectedDish(selectedDish);
        storedMenu = null;
    }

    private void loadOrderMenu() {
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        dishesXAdapter.notifyDataSetChanged();
    }

    private List<Mark> sortMarks(List<Mark> unsortedMarks) {
        List<Mark> sortedMarks = new ArrayList<Mark>();
        for (int i = 0; i < unsortedMarks.size(); i++) {
            Mark mark = unsortedMarks.get(i);
            boolean inserted = false;
            for (int j = 0; j < sortedMarks.size(); j++) {
                if (sortedMarks.get(j).getState() > mark.getState()) {
                    sortedMarks.add(j, mark);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                sortedMarks.add(mark);
            }
        }
        return sortedMarks;
    }

    private void initShortCutMarks() {
        shortCutMarks = new ArrayList<Mark>(3);
        List<Mark> marks = new ArrayList<Mark>();
        marks.add(allMarks.size() < 1 ? new Mark("M1") : allMarks.get(0));
        marks.add(allMarks.size() < 2 ? new Mark("M2") : allMarks.get(1));
        markShorCutXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
        markShorCutXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdMarksGrid.setAdapter(markShorCutXAdapter);
    }

    private void clearOrderMenu() {
        //bk and clear selected menu
        bkOfLastSelection = new ArrayList<SelectionDetail>();
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            bkOfLastSelection.add(selectionDetail);
        }
        CustomerSelection.getInstance().clearMenu();

        //bk and clear bkOfLastTable
        bkOfLastTable = odIdTableTbtn.getText();
        odIdTableTbtn.setText("");
        odIdTableTbtn.setChecked(true);
        loadOrderMenu();

    }

    /**no one is calling this mehtod now, because we not response feels like app goes wrong. and user might input again.
     //waiting 15 seconds or untile comfirmPrintOK() is called.
     private void waitForPrintSuccess(){
     int i = 0;
     while (true){
     i++;
     AppUtils.sleep(1000);
     if( bkOfLastSelection == null && "TB".equals(bkOfLastTable)){
     return;
     }
     }
     }

     //no one is calling this mehtod now, because we relized the order will not lost, it stay in ipConentMap
     //when connection come back, it will be printed out.
     private void rollbackLastOrder() {
     //save the menu into database.
     for(SelectionDetail selectionDetail : bkOfLastSelection){
     CustomerSelection.getInstance().addSelectedDish(selectionDetail);
     }

     //clear selected menu
     odIdTableTbtn.setText(bkOfLastTable);
     odIdTableTbtn.setChecked(false);
     loadOrderMenu();

     }*/

    public static void comfirmPrintOK(){

        if(bkOfLastSelection == null){
            return;
        }

        //save the menu into database.
        SaleRecordDao saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        for(SelectionDetail selectionDetail : bkOfLastSelection){
            int number = selectionDetail.getDishNum();
            Menu menu = selectionDetail.getDish();
            String name = menu.getMname();
            Double price = menu.getPrice() * number;

            //if set as conbine mark price into dish price.
            if("true".equals(AppData.getCustomData("conbineMarkPrice"))) {
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
            saleRecord.setMname(name);
            saleRecord.setNumber(Double.valueOf(number));
            saleRecord.setPrice(price);
            saleRecordDao.insertOrReplace(saleRecord);

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
                        }
                    }
                }
            }
        }
        bkOfLastSelection = null;
        bkOfLastTable = "TB";

        isCancel = false;
    }
}
