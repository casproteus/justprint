package com.just.print.ui.holder;

import android.content.Context;
import android.widget.ToggleButton;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigPrintReportViewHolder extends XViewHolder<Mark> {
    @Override
    public int getLayoutId() {
        return R.layout.tag_view_holder;
    }

    @XViewByID(R.id.tag)
    ToggleButton tvMark;

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
         setOnClickListener(this);
        tvMark.setClickable(false);
        tvMark.setTextOn(null);
        tvMark.setTextOff(null);
        tvMark.setChecked(true);
        tvMark.setCompoundDrawablePadding(10);
        tvMark.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.forum_tag_close, 0);
    }

    @Override
    public void onResetView(Mark mark, int i) {
        tvMark.setText(mark.getName());
     }
}
