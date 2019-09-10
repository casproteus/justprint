package com.just.print_night.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print_night.R;
import com.just.print_night.db.bean.Menu;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigMenuViewHolder extends XViewHolder<Menu> {
    @XViewByID(R.id.mname)
    TextView mname;
    @XViewByID(R.id.msg)
    TextView msg;

    @Override
    public int getLayoutId() {
        return R.layout.config_menu_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.modifyMenu).setOnClickListener(this);
        findViewById(R.id.delMenu).setOnClickListener(this);
    }

    @Override
    public void onResetView(Menu menu, int i) {
        mname.setText(menu.getMname());
        msg.setText(menu.getID());
        if (menu.getM2M_MenuPrintList() != null)
            msg.append("   P" + menu.getM2M_MenuPrintList().size());

        msg.append("   $" + menu.getPrice());

    }
}
