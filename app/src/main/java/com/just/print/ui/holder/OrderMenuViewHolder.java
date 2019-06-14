package com.just.print.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.just.print.sys.model.SelectionDetail;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

/**
 * Created by qiqi on 2016/11/9.
 */

public class OrderMenuViewHolder extends XViewHolder<SelectionDetail> {
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

    SelectionDetail menu;

    @Override
    public void onResetView(SelectionDetail menu, int i) {
        this.menu = menu;
        odMId.setText(menu.getDish().getID());
        odMName.setText(menu.getDish().getMname());
//        L.d(TAG, Integer.toString(menu.getDishNum()));
        odMNNum.setText(Integer.toString(menu.getDishNum()));
        odMnLoutMarkTv.setText("");
        List<Mark> marks = menu.getMarkList();
        if (marks != null) {
            for (int j = 0; j < marks.size(); j++) {
                Mark mark = marks.get(j);

                if (j == 0) {
                    odMnLoutMarkTv.setText(mark.toString());
                } else {
                    odMnLoutMarkTv.append(" " + mark.toString());
                }
            }
        }
    }
}
