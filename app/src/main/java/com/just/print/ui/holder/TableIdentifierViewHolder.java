package com.just.print.ui.holder;

import android.content.Context;
import android.widget.Button;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by caspr on 6/12/2019.
 */

public class TableIdentifierViewHolder extends XViewHolder<Mark>{
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
    public void onResetView(Mark s, int i) {
        textView.setText(s.getName());
    }
}
