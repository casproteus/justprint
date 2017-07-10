package com.just.print.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Menu;
import com.stupid.method.adapter.expand.XExpadnViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by Administrator on 2016/11/17.
 */

public class SubTitleMenuExpandViewHolder extends XExpadnViewHolder<Menu> {

    @XViewByID(R.id.title)
    TextView title;
    @XViewByID(R.id.button)
    Button button;

    @Override
    protected void onNodeIsChild(boolean b, int i) {

    }

    @Override
    protected void onNodeIsParent(boolean b, int i) {

    }


    @Override
    public int getLayoutId() {
        return R.layout.title_subtitle;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        button.setOnClickListener(this);
    }

    @Override
    public void onResetView(Menu menu, int i) {
        title.setText(menu.getMname());
    }
}
