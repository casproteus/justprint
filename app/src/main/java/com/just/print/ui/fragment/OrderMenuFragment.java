package com.just.print.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.app.EventBus;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.util.L;
import com.stupid.method.adapter.IXDataListener;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;


public class OrderMenuFragment extends BaseFragment implements View.OnClickListener, OnClickItemListener, EventBus.EventHandler {
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";

    private static final String TAG = "OrderMenuFragment";

    @XViewByID(R.id.orderMenuList)
    ListView odMenuList;
    @XViewByID(R.id.printBtn)
    Button pntButton;
    @XViewByID(R.id.odMnTableNumEt)
    private EditText odMnTableNumEt;


    private XAdapter2<SelectionDetail> menuXAdapter;
    private List<SelectionDetail> menuList;

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (EVENT_ADD_MENU.equals(eventName)) {
            loadOrderMenu();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.order_menu_fragment;
    }


    @Override
    public void onCreated(Bundle savedInstanceState) {

        getEventBus().register(EVENT_ADD_MENU, this);
        menuList = new ArrayList<SelectionDetail>();

//        SelectionDetail testMenu = new SelectionDetail();
//        List<String> tmpList = new ArrayList<String>();
//        tmpList.add("多麻");
//        testMenu = new SelectionDetail();
//        testMenu.setDishID("B03");
//        testMenu.setDishName("陈氏红烧肉");
//        testMenu.setDishNum(1);
//        testMenu.setMarkList(tmpList);
//        for (int i = 0; i < 10; i++) {
//            menuList.add(testMenu);
//        }

        new StupidReflect(this, getView()).init();
        pntButton.setOnClickListener(this);
        odMnTableNumEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CustomerSelection.getInstance().setTableNumber(odMnTableNumEt.getText().toString());
            }
        });
        menuXAdapter = new XAdapter2<SelectionDetail>(getContext(), OrderMenuViewHolder.class);
        menuXAdapter.setClickItemListener(this);
        menuXAdapter.setData(menuList);
        menuXAdapter.setOnDataChang(new IXDataListener() {
            View noDataShowMe = findViewById(R.id.noDataShowMe);

            @Override
            public void onDataChange() {
                if (noDataShowMe.getVisibility() == View.VISIBLE)
                    noDataShowMe.setVisibility(View.GONE);
            }

            @Override
            public void onDataEmpty() {
                if (noDataShowMe.getVisibility() == View.GONE)
                    noDataShowMe.setVisibility(View.VISIBLE);
            }
        });
        odMenuList.setAdapter(menuXAdapter);
    }


    private void loadOrderMenu() {
        menuXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        menuXAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickItem(View view, int i) {
        int num;
        switch (view.getId()) {
            case R.id.odmnadd:
                num = menuXAdapter.get(i).getDishNum();
                menuXAdapter.get(i).setDishNum(++num);
                loadOrderMenu();
                break;
            case R.id.odmnreduce:
                num = menuXAdapter.get(i).getDishNum();
                if (num > 0) {
                    num--;
                }
                menuXAdapter.get(i).setDishNum(num);
                loadOrderMenu();
                break;
            case R.id.oddelDish:

                CustomerSelection.getInstance().deleteSelectedDish(i);
                loadOrderMenu();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printBtn:
                System.out.println("Unexpected code executed!!!!!!");
                Thread.dumpStack();
                L.d(TAG, "print Start");
                //WifiPrintService.getInstance().exePrintCommand(CustomerSelection.getInstance().getSelectedDishes());
                break;
        }
    }
}
