package com.just.print.ui.holder;

import android.content.Context;
import android.widget.ToggleButton;

import com.just.print.R;
import com.just.print.db.bean.Mark;
import com.just.print.util.L;
import com.stupid.method.adapter.XViewHolder;

/**
 * Created by qiqi on 2016/11/21.
 */

public class OrderIdentifierMarkViewHolder extends XViewHolder<Mark>{
    private static final String TAG = "OrderIdMarkViewHolder";
    ToggleButton textView;
//    @Override
//    public View getView() {
//        if (super.getView() == null) {
//            textView = new TextView(context);
//            textView.setPadding(10, 10, 10, 10);
//            textView.setGravity(Gravity.CENTER);
//            SquareLayout sl = new SquareLayout(context);
//            SquareLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//            sl.addView(textView, params);
//            return sl;
//        }
//        return super.getView();
//    }

    @Override
    public int getLayoutId() {
        return R.layout.tag_identifier_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        textView = (ToggleButton) findViewById(R.id.tagid);
        textView.setTextOff(null);
        textView.setTextOn(null);
        textView.setOnClickListener(this);
    }

    @Override
    public void onResetView(Mark mark, int i) {
        L.d(TAG,"onResetView");
        L.d(TAG,"hashcode: " + String.valueOf(mark.hashCode()));
        L.d(TAG,"item: " + String.valueOf(i) + "select: " + String.valueOf(mark.select));
        textView.setText(mark.getName());
        textView.setChecked(mark.select);
    }
}
