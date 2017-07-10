package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Menu;
import com.just.print.sys.model.DishesDetailModel;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by qiqi on 2016/11/15.
 */

public class OrderIdentifierViewHolder extends XViewHolder<Menu> {
    @XViewByID(R.id.odIdCName)
    private TextView odIdCName;
    @XViewByID(R.id.odIdCID)
    private TextView odIdCID;
    @Override
    public int getLayoutId() {

        return R.layout.order_identifier_view_holder;
    }
    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        setOnClickListener(this);
    }

    @Override
    public void onResetView(Menu menu, int i) {
        odIdCID.setText(menu.getID());
        odIdCName.setText(menu.getMname());
    }
}
