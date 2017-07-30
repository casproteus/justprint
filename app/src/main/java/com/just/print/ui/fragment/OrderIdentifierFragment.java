package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.app.EventBus;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.activity.ConfigActivity;
import com.just.print.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print.ui.holder.OrderIdentifierViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.util.L;
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


public class OrderIdentifierFragment extends BaseFragment implements View.OnClickListener, OnClickItemListener, EventBus.EventHandler, WifiPrintService.StatusDisplayer {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";
    private static final String TAG = "OrderIdentifierFragment";
    @XViewByID(R.id.odIdFrLoutMenuList)
    private ListView odIdFrLoutMenuList;
    @XViewByID(R.id.odIdInput)
    private TextView odIdInput;
    @XViewByID(R.id.odIdfrName)
    private TextView odIdfrName;
    //@XViewByID(R.id.odIdLoutMarksGv)
    //private GridView odIdLoutMarksGv;
    @XViewByID(R.id.odIdLoutItemsGv)
    private GridView odIdLoutItemsGv;
    @XViewByID(R.id.odIdMarksGrid)
    private GridView odIdMarksGrid;
    @XViewByID(R.id.odIdTableTbtn)
    ToggleButton odIdTableTbtn;
    //@XViewByID(R.id.odIdDefaultTbtn)
    //ToggleButton odIdDefaultTbtn;
    private Menu storedMenu;
    XAdapter2<Mark> markXAdapter;
    XAdapter2<Menu> menuXAdapter;
    private XAdapter2<SelectionDetail> dishesXAdapter;
    XAdapter2<String> itemXAdapter;
    List<String> itemList;
    int curmarkitem;
    List<Mark> markselect;

    static int times = 0;

    @XClick({R.id.odIdConfigBtn, R.id.odIdDeliveryBtn, R.id.odIdSndBtn, R.id.odIdDelBtn, R.id.odIdOkBtn})
    private void exeControlCommand(View v) {
        switch (v.getId()) {
            case R.id.odIdConfigBtn:
                startActivity(new Intent(getContext(), ConfigActivity.class));
                break;
            case R.id.odIdDeliveryBtn:
                odIdTableTbtn.setText("Delivery");
                CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                break;
            case R.id.odIdSndBtn:
                String result = WifiPrintService.getInstance().exePrintCommand();
                if("2".equals(result)){
                    times++;
                    if(times < 1) {
                        showToast("The content of last time is not printed yet. please wait and try again.");
                    }else if(times < 2){
                        showToast("Please restart the device which blocked the printer, then try again!");
                    }else{
                        WifiPrintService.getInstance().reInitPrintRelatedMaps();
                        if("0".equals(WifiPrintService.getInstance().exePrintCommand())){
                            times = 0;
                            clearOrderMenu();
                        }
                        showToast("Please check and make sure last order is printed out!");
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
                if (odIdTableTbtn.isChecked() == false) {
                    if (null != storedMenu) {
                        addDish();
                        odIdInput.setText("");
                        odIdfrName.setText("");
                        if (dishesXAdapter != null) {
                            loadOrderMenu();
                        }
                    }
                } else {
                    odIdTableTbtn.setChecked(false);
                    CustomerSelection.getInstance().setTableNumber(odIdTableTbtn.getText().toString());
                }
                break;
        }

    }

    IXOnItemClickListener itemXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            L.d(TAG, "onClickItem1");
            Menu tmpMenu;
            L.d(TAG, String.valueOf(view.getId()) + String.valueOf(i));
            switch (view.getId()) {
                case R.id.buttonholder:
                    switch (i) {
                        case 18:
                            DelOneText();
                            break;
                        case 19:
                            if (null != storedMenu) {
                                addDish();
                                if (dishesXAdapter != null) {
                                    loadOrderMenu();
                                }
                            }
                            break;
                        case 20:
                            String result = WifiPrintService.getInstance().exePrintCommand();
                            if ("2".equals(result)) {
                                showToast("The content of last time is not printed yet. please wait and try again.");
                            } else{
                                clearOrderMenu();
                            }
                            break;
                        default:
                            if (i < 10) {
                                InputText(Integer.toString((i + 1) % 10));
                            } else {
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                            break;
                    }
                    tmpMenu = SearchMenuFromDB(odIdInput.getText().toString());
                    if (null != tmpMenu) {
                        odIdfrName.setText(tmpMenu.getMname());
                    } else {
                        odIdfrName.setText("");
                    }
                    break;
                case R.id.tagid:
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
        L.d(TAG, "onCreated");
        getEventBus().register(EVENT_ADD_MENU, this);
        new StupidReflect(this, getView()).init();
        //设置餐桌号用
        //CustomerSelection.getInstance().setTableNumber(odIdTableNumEt.getText().toString());
        storedMenu = null;
        String[] items = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F"};
        itemList = new ArrayList<String>(Arrays.asList(items));
        itemXAdapter = new XAdapter2<String>(getActivity(), itemList, OrderIdentifierItemViewHolder.class);
        itemXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdTableTbtn.setTextOn(null);
        odIdTableTbtn.setTextOff(null);
        odIdTableTbtn.setText("TB");
        odIdTableTbtn.setOnClickListener(this);
        //odIdDefaultTbtn.setTextOn(null);
        //odIdDefaultTbtn.setTextOff(null);
        //odIdDefaultTbtn.setText("MDF");
        //odIdDefaultTbtn.setOnClickListener(this);

        menuXAdapter = new XAdapter2<Menu>(getContext(), OrderIdentifierViewHolder.class);
        //menuXAdapter.setClickItemListener(menuAdapterClick);
        dishesXAdapter = new XAdapter2<SelectionDetail>(getContext(), OrderMenuViewHolder.class);
        dishesXAdapter.setClickItemListener(this);
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());

        odIdFrLoutMenuList.setAdapter(dishesXAdapter);
        markselect = new ArrayList<Mark>(3);
        List<Mark> markList = DaoExpand.queryNotDeleteAll(getDaoMaster().newSession().getMarkDao());
        List<Mark> marks = new ArrayList<Mark>();
        //添加Mark列表
        marks.add(markList.size() > 0 ? markList.get(0) : new Mark("M1"));
        marks.add(markList.size() > 1 ? markList.get(1) : new Mark("M2"));
        markXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
        markXAdapter.setClickItemListener(this.itemXAdapterClick);
        //odIdLoutMarksGv.setAdapter(markXAdapter);
        odIdMarksGrid.setAdapter(markXAdapter);
        odIdLoutItemsGv.setAdapter(itemXAdapter);
        //注册打印机消息监听
        WifiPrintService.getInstance().registPrintState(this);
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
            /*
            case R.id.odIdDefaultTbtn:
                showMarksDialog(markselect,new onChoiceMarks(){
                    @Override
                    public void onChoiceMarks(List<Mark> result){

                    }
                });
                */
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
        List<Menu> mnlist = DaoExpand.queryFuzzyMenu(getDaoMaster().newSession().getMenuDao(), context);
        if (mnlist.size() >= 1) {
            //对列表进行排序
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
                dishesXAdapter.remove(a);
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
        List<Mark> markList = DaoExpand.queryNotDeleteAll(getDaoMaster().newSession().getMarkDao());
        List<Mark> marks = new ArrayList<Mark>();
        //添加Mark列表
        marks.add(markList.size() > 0 ? markList.get(0) : new Mark("M1"));
        marks.add(markList.size() > 1 ? markList.get(1) : new Mark("M2"));
        markXAdapter.setData(marks);
        markXAdapter.notifyDataSetChanged();
    }

    private void clearOrderMenu() {
        L.d(TAG, "clearOrderMenu start...");
        CustomerSelection.getInstance().clearMenu();
        odIdTableTbtn.setText("");
        loadOrderMenu();
    }

    public void showStatus(String src, int i) {
        switch (i) {
            case 2:     //WFPRINTER_CONNECTEDERR
                showToast("打印机:" + src + " 连接错误");
                break;
            case 4:     //COMMON
                showToast(src);
        }

    }
}
