package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;

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
import com.just.print.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.util.AppUtils;
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
    public static OrderIdentifierFragment getInstance(){
        if(instance == null){
            instance = new OrderIdentifierFragment();
        }
        return instance;
    }

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

    private Menu storedMenu;

    XAdapter2<Mark> markXAdapter;
    XAdapter2<Menu> menuXAdapter;
    XAdapter2<SelectionDetail> dishesXAdapter;
    XAdapter2<String> itemXAdapter;

    int curmarkitem;
    List<Mark> markselect;
    public static List<SelectionDetail> bkOfLastSelection;
    private static CharSequence bkOfLastTable;
    static int times = 0;
    String[] items = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "F", "H", "+","togo"};

    @XClick({R.id.odIdConfigBtn, R.id.odIdSndBtn, R.id.odIdDelBtn, R.id.odIdOkBtn})
    private void exeControlCommand(View v) {
        switch (v.getId()) {
            case R.id.odIdConfigBtn:
                startActivity(new Intent(getContext(), ConfigActivity.class));
                break;
            case R.id.odIdSndBtn:
                List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
                if(selectionDetails == null || selectionDetails.size() == 0){
                    showToast("Nothing selected!");
                    return;
                }

                String result = WifiPrintService.getInstance().exePrintCommand();
                if(WifiPrintService.ERROR.equals(result)){
                    if(times < 1) {
                        showToast("Last print not done yet. Please wait.");
                        times++;
                    }else if(times < 2){
                        showToast("Last print not done yet. Press Send Again To Resend it!");
                        times++;
                    }else{
                        WifiPrintService.getInstance().reInitPrintRelatedMaps();
                        if(WifiPrintService.SUCCESS.equals(WifiPrintService.getInstance().exePrintCommand())){
                            times = 0;
                            clearOrderMenu();
                        }
                        showToast("Order was re-send!");
                    }
                }else {
                    times = 0;
                    clearOrderMenu();
                }
                break;
            case R.id.odIdDelBtn:
                DelOneText();
                break;
            case R.id.odIdOkBtn:
                if (odIdTableTbtn.isChecked()) {    //waiting for input table num status. always allowed!
                    odIdTableTbtn.setChecked(false);
                    CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                } else {                            //inputting dishes status. need to check print status.

                    if(bkOfLastSelection != null){      //content not all send to printer yet.
                        //if last order has only one dish and it happened to be same
                        //as the one selected this time, waiter will think the roll back is not happenning.
                        //we decided to not display last order--- rollbackLastOrder();
                        ToastUtil.showToast("Printing...Please wait.");
                        return;
                    }else if (storedMenu != null) {     //last order send to printer well.
                        addDish();
                        odIdInput.setText("");
                        odIdfrName.setText("");
                        if (dishesXAdapter != null) {
                            loadOrderMenu();
                        }
                    }
                }
                break;
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
                        case 14:
                            if(items.length == 18) {
                                InputText("F");
                            }else{
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                            break;
                        case 15:
                            if(items.length == 18) {
                                InputText("H");
                            }else{
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                            break;
                        default:
                            if (i < 10) {
                                InputText(Integer.toString((i + 1) % 10));
                            } else if( i == items.length - 2){
                                InputText("+");
                            } else if (i == items.length - 1) {
                                odIdTableTbtn.setText("TOGO");
                                CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
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
                    Mark m = markXAdapter.getItem(i);
                    L.d(TAG, String.valueOf(m.select));
                    if (m.select) {
                        m.select = false;
                        markselect.remove(m);
                    } else {
                        m.select = true;
                        markselect.add(m);
                    }
                    L.d(TAG, String.valueOf(m.select));
                    if (CustomerSelection.getInstance().getSelectedDishes() != null && CustomerSelection.getInstance().getSelectedDishes().size() > 0) {
                        int size = CustomerSelection.getInstance().getSelectedDishes().size();
                        CustomerSelection.getInstance().getSelectedDishes().get(size - 1).setMarkList(markselect);
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
        if(!StringUtils.isBlank(AppData.getCustomData(AppData.KEY_CUST_LAST_CHAR))) {
            if(AppData.getCustomData(AppData.KEY_CUST_LAST_CHAR).equalsIgnoreCase("P")){
                items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N","O","P", "+", "togo"};
            }else if(AppData.getCustomData(AppData.KEY_CUST_LAST_CHAR).equalsIgnoreCase("N")){
                items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N", "+", "togo"};
            }
        }
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
        markselect = new ArrayList<Mark>(3);
        List<Mark> markList = DaoExpand.queryNotDeletedAll(Applic.getMarkDao());
        List<Mark> marks = new ArrayList<Mark>();
        marks.add(markList.size() > 0 ? markList.get(0) : new Mark("M1"));
        marks.add(markList.size() > 1 ? markList.get(1) : new Mark("M2"));
        markXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
        markXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdMarksGrid.setAdapter(markXAdapter);
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
                showMarksDialog(dishesXAdapter.get(i).getMarkList(), new onChoiceMarks() {
                    @Override
                    public void onChoiceMarks(List<Mark> result) {
                        L.d(TAG, result.toString());
                        dishesXAdapter.get(curmarkitem).setMarkList(result);
                        loadOrderMenu();
                    }
                });

                break;
        }
    }

    private void addDish() {
        SelectionDetail ddm = new SelectionDetail();
        ddm.setDish(storedMenu);
        ddm.setMarkList(new ArrayList<Mark>());
        L.d(TAG, markselect.toString());
        ddm.setDishNum(1);
        CustomerSelection.getInstance().addSelectedDish(ddm);
        storedMenu = null;
        //markselect.clear();
        markselect = new ArrayList<Mark>();
        markselect.clear();
        markXAdapter.notifyDataSetChanged();
        /*  clear Convenience Mark Area */
        resetConvenienceMarkArea();
    }

    private void loadOrderMenu() {
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        dishesXAdapter.notifyDataSetChanged();
    }

    private void resetConvenienceMarkArea() {
        List<Mark> markList = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getMarkDao());
        List<Mark> marks = new ArrayList<Mark>();
        //添加Mark列表
        marks.add(markList.size() > 0 ? markList.get(0) : new Mark("M1"));
        marks.add(markList.size() > 1 ? markList.get(1) : new Mark("M2"));
        markXAdapter.setData(marks);
        markXAdapter.notifyDataSetChanged();
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
