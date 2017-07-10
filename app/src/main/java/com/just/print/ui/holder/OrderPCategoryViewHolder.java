package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Category;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.adapter.expand.XExpadnViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/11/2.
 */
public class OrderPCategoryViewHolder extends XExpadnViewHolder<Category> {
    @XViewByID(R.id.cname)
    TextView cname;

    @Override
    public int getLayoutId() {
        return R.layout.config_category_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        setOnClickListener(this);
        findViewById(R.id.delCategory).setOnClickListener(this);
    }

    @Override
    public void onResetView(Category category, int i) {
        cname.setText(category.getCname());
    }

    @Override
    protected void onNodeIsParent(boolean b, int i) {

    }

    @Override
    protected void onNodeIsChild(boolean b, int i) {

    }
}
