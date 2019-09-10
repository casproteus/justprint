package com.just.print.ui.holder;

import android.content.Context;
import android.widget.ToggleButton;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by Administrator on 2016/11/17.
 */

public class MarkViewHolder extends XViewHolder<Mark> {
    @Override
    public int getLayoutId() {
        return R.layout.tag_view_holder;
    }

    ToggleButton textView;


    @Override
    public void onCreate(Context context) {
        textView = (ToggleButton) findViewById(R.id.tag);
        textView.setTextOff(null);
        textView.setTextOn(null);
        textView.setOnClickListener(this);
    }

    @Override
    public void onResetView(Mark mark, int i) {
        textView.setText(mark.getName());
        textView.setChecked(mark.select);
    }
}
