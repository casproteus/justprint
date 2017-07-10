package com.just.print.ui.holder;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.ui.SquareLayout;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by qiqi on 2016/11/23.
 */

public class OrderIdentifierItemViewHolder extends XViewHolder<String>{
    Button textView;
    @Override
    public void onCreate(Context context) {
        textView = (Button)findViewById(R.id.buttonholder);
        textView.setOnClickListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_view_holder;
    }

    @Override
    public void onResetView(String s, int i) {
        textView.setText(s);
    }
}
