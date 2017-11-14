package com.just.print.ui.holder;

import android.content.Context;
import android.widget.Button;

import com.just.print.R;
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
        textView.setTextSize(24);
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_view_holder;
    }

    @Override
    public void onResetView(String s, int i) {
        textView.setText(s);
//        try {
//            Integer.valueOf(s);
//        }catch(Exception e){
//            textView.setBackgroundColor(Color.rgb(100, 100, 100));
//
//            //textView.setShadowLayer(5f, 3f, 3f, 0xFFFF00FF);
//        }
    }
}
