package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Printer;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/11/23.
 */
public class AddPrintViewHolder extends XViewHolder<Printer> {
    @XViewByID(R.id.ip)
    TextView ip;
    @XViewByID(R.id.name)
    TextView name;

    @Override
    public int getLayoutId() {
        return R.layout.add_print_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.imageButton).setOnClickListener(this);
    }

    @Override
    public void onResetView(Printer printer, int i) {
        ip.setText(printer.getIp());
        name.setText(printer.getPname());
    }
}
