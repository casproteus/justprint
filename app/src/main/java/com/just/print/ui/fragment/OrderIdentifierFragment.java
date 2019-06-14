package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.app.EventBus;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.activity.ConfigActivity;
import com.just.print.ui.holder.OrderMarksSelectionViewHolder;
import com.just.print.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
import com.just.print.util.ToastUtil;
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
import java.util.List;


public class OrderIdentifierFragment extends BaseFragment implements View.OnClickListener, OnClickItemListener, EventBus.EventHandler{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";
    private static final String TAG = "OrderIdentifierFragment";

    private static OrderIdentifierFragment instance = null;
    private static boolean isCancel;
    private List<Mark> allMarkList;
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

    List<Mark> markShotCutSelection;
    public static List<SelectionDetail> bkOfLastSelection;
    private static CharSequence bkOfLastTable;
    static int times = 0;
    String[] items = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "H", "+", "togo", "canc"};

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
                if( viewSwitcher.getDisplayedChild() == 0) {
                    if (odIdTableTbtn.isChecked()) {    //waiting for input table num status. always allowed!
                        odIdTableTbtn.setChecked(false);
                        CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                    } else {                            //inputting dishes status. need to check print status.

                        if (bkOfLastSelection != null) {      //content not all send to printer yet.
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

    private void updateSelectionOfCurrentDish() {
        for(int i = 0; i < allMarkList.size(); i++){
            Mark mark = allMarkList.get(i);
            boolean matched = false;
            for(int j = 0; j < marksOfCurDish.size(); j++){
                if(marksOfCurDish.get(j).getName().equals(mark.getName())){
                    marksOfCurDish.get(j).setQt(mark.getQt());
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
                    switch (i) {
                        case 16:
                            if(items.length == 20) {
                                InputText("H");
                            }else{
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                            break;
                        default:
                            if (i < 10) {
                                InputText(Integer.toString((i + 1) % 10));
                            } else if( i == items.length - 3){
                                InputText("+");
                            } else if (i == items.length - 2) {
                                odIdTableTbtn.setText("TOGO");
                                CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                            } else if (i == items.length - 1) {
                                odIdTableTbtn.setText("");
                                //added the selected dish with negative price.
                                if(printCurrentSelection(true)){    //arranged to print---didn't met the case that previous print not finished yet.
                                    isCancel = true;
                                }
                            }else {
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                            break;
                    }
                    if(!odIdTableTbtn.isChecked()){
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
                    //Mark Press
                    Mark selectedMark = markShorCutXAdapter.getItem(i);
                    L.d(TAG, String.valueOf(selectedMark.select));
                    if (selectedMark.select) {
                        selectedMark.select = false;
                        markShotCutSelection.remove(selectedMark);
                    } else {
                        selectedMark.select = true;
                        markShotCutSelection.add(selectedMark);
                    }
                    L.d(TAG, String.valueOf(selectedMark.select));
                    if (CustomerSelection.getInstance().getSelectedDishes() != null && CustomerSelection.getInstance().getSelectedDishes().size() > 0) {
                        int size = CustomerSelection.getInstance().getSelectedDishes().size();
                        CustomerSelection.getInstance().getSelectedDishes().get(size - 1).setMarkList(markShotCutSelection);
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
        items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E"};
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

        //添加Mark列表
        initShortCutMarkArea();

        //init MarkSelection panel
        markAllXAdapter = new XAdapter2<Mark>(getContext(), OrderMarksSelectionViewHolder.class);
        markAllXAdapter.setClickItemListener(this);
        markAllXAdapter.setData(allMarkList);
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

        for(int j = 0; j < allMarkList.size(); j++) {
            Mark mark = allMarkList.get(j);
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


        markAllXAdapter.setData(allMarkList);
        markAllList.setAdapter(markAllXAdapter);

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
        L.d(TAG, markShotCutSelection.toString());
        selectedDish.setDishNum(1);
        CustomerSelection.getInstance().addSelectedDish(selectedDish);
        storedMenu = null;
        //markShotCutSelection.clear();
        markShotCutSelection = new ArrayList<Mark>();
        markShotCutSelection.clear();
        markShorCutXAdapter.notifyDataSetChanged();
        /*  clear Convenience Mark Area */
        initShortCutMarkArea();
    }

    private void loadOrderMenu() {
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        dishesXAdapter.notifyDataSetChanged();
    }

    private void initShortCutMarkArea() {
        markShotCutSelection = new ArrayList<Mark>(3);
        allMarkList = DaoExpand.queryNotDeletedAll(Applic.getMarkDao());
        List<Mark> marks = new ArrayList<Mark>();
        if(allMarkList.size() == 0){
            marks.add(new Mark("M1"));
            marks.add(new Mark("M2"));
        }else{
            for(int i = 0; i < allMarkList.size(); i++){
                Mark mark = allMarkList.get(i);
                boolean inserted = false;
                for(int j = 0; j < marks.size(); j++){
                    if(marks.get(j).getState() > mark.getState()){
                        marks.add(j, mark);
                        inserted = true;
                        break;
                    }
                }
                if(!inserted) {
                    marks.add(mark);
                }
            }
            if(marks.size() > 2){
                for(int i = 2; i < marks.size(); i++){
                    marks.remove(i);
                }
            }
        }
        markShorCutXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
        markShorCutXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdMarksGrid.setAdapter(markShorCutXAdapter);



//
//        List<Mark> markList = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getMarkDao());
//        List<Mark> marks = new ArrayList<Mark>();
//        //添加Mark列表
//        marks.add(markList.size() > 0 ? markList.get(0) : new Mark("M1"));
//        marks.add(markList.size() > 1 ? markList.get(1) : new Mark("M2"));
//        markShorCutXAdapter.setData(marks);
//        markShorCutXAdapter.notifyDataSetChanged();
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
            List<Mark> marks = selectionDetail.getMarkList();
            if(marks != null){
                for (Mark mark : marks) {
                    price += ((float)mark.getVersion())/100.0;
                }
            }

            if(isCancel){
                isCancel = false;
                price *= -1;
            }
            SaleRecord saleRecord = new SaleRecord();
            saleRecord.setMname(name);
            saleRecord.setNumber(Double.valueOf(number));
            saleRecord.setPrice(price);
            saleRecordDao.insertOrReplace(saleRecord);
        }
        bkOfLastSelection = null;
        bkOfLastTable = "TB";
    }
}
