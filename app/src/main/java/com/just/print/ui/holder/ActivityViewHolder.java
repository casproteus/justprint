package com.just.print.ui.holder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.just.print.R;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by wangx on 2016/11/2.
 */
public class ActivityViewHolder extends XViewHolder<ActivityInfo> {

    TextView textView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        setOnClickListener(this);
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        try {
            context.startActivity(new Intent(context, Class.forName(info.name)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    ActivityInfo info;

    @Override
    public void onResetView(ActivityInfo info, int i) {
        this.info = info;
        StringBuilder sb = new StringBuilder();
        sb.append(info.name).append("\r\n");
        textView.setText(sb.toString());
    }
}
