package com.just.print.ui.holder;

import android.content.Context;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Printer;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/10/31.
 */
public class ConfigPrinterViewHolder extends XViewHolder<Printer> {
    @XViewByID(R.id.ip)
    TextView ip;
    @XViewByID(R.id.name)
    TextView name;

    @Override
    public int getLayoutId() {
        return R.layout.printer_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.modify).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
    }

    @Override
    public void onResetView(Printer printer, int i) {
        ip.setText(printer.getIp());

        if (printer.getFirstPrint() != null && printer.getFirstPrint() == 1) {
            name.setText("[主] " + printer.getPname());

        } else
            name.setText(printer.getPname());

    }
}
