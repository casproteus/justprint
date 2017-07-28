package com.just.print.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.sys.model.DishesDetailModel;
import com.just.print.util.L;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import org.w3c.dom.Text;

/**
 * Created by qiqi on 2016/11/9.
 */

public class OrderMenuViewHolder extends XViewHolder<DishesDetailModel> {
    private static final String TAG = "OrderMenuViewHolder";
    @XViewByID(R.id.odmId)
    TextView odMId;
    @XViewByID(R.id.odmname)
    TextView odMName;
    @XViewByID(R.id.odmnnum)
    TextView odMNNum;
    @XViewByID(R.id.odMnLoutMarkTv)
    TextView odMnLoutMarkTv;

    @Override
    public int getLayoutId() {

        return R.layout.order_menu_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.oddelDish).setOnClickListener(this);
        findViewById(R.id.odmnadd).setOnClickListener(this);
        findViewById(R.id.odmnreduce).setOnClickListener(this);
        findViewById(R.id.odMnLoutMarkBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.odmnadd:
                menu.setDishNum(menu.getDishNum() + 1);
                odMNNum.setText(Integer.toString(menu.getDishNum()));
                break;
            case R.id.odmnreduce:
                if (menu.getDishNum() > 1) {
                    menu.setDishNum(menu.getDishNum() - 1);
                    odMNNum.setText(Integer.toString(menu.getDishNum()));
                }
                break;
            default:
                super.onClick(v);
        }

    }

    DishesDetailModel menu;

    @Override
    public void onResetView(DishesDetailModel menu, int i) {
        this.menu = menu;
        odMId.setText(menu.getDish().getID());
        odMName.setText(menu.getDish().getMname());
        L.d(TAG, Integer.toString(menu.getDishNum()));
        odMNNum.setText(Integer.toString(menu.getDishNum()));
        odMnLoutMarkTv.setText("");
        if (menu.getMarkList() != null) {
            for (int j = 0; j < menu.getMarkList().size(); j++) {
                if (j == 0) {
                    odMnLoutMarkTv.setText(menu.getMarkList().get(j).getName());
                } else {
                    odMnLoutMarkTv.append(" " + menu.getMarkList().get(j));
                }
            }
        }
    }
}
