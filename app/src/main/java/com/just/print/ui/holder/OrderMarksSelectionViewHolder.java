package com.just.print.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.db.bean.Mark;
import com.stupid.method.adapter.XViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by caspr on 6/12/2019.
 */

public class OrderMarksSelectionViewHolder extends XViewHolder<Mark> {

    private static OrderMarksSelectionViewHolder instance = null;

    public static OrderMarksSelectionViewHolder getInstance(){
        if(instance == null){
            instance = new OrderMarksSelectionViewHolder();
        }
        return instance;
    }

    @XViewByID(R.id.markText)
    TextView markName;
    @XViewByID(R.id.markPrice)
    TextView markPrice;
    @XViewByID(R.id.markSelectedNum)
    TextView markSelectedNum;

    Mark mark;

    @Override
    public int getLayoutId() {
        return R.layout.order_marks_selection_view_holder;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        findViewById(R.id.markQtAdd).setOnClickListener(this);
        findViewById(R.id.markQtReduce).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.markQtAdd:
                mark.setQt(mark.getQt() + 1);
                markSelectedNum.setText(Integer.toString(mark.getQt()));
                break;
            case R.id.markQtReduce:
                if (mark.getQt() > 0) {
                    mark.setQt(mark.getQt() - 1);
                    markSelectedNum.setText(Integer.toString(mark.getQt()));
                }
                break;
            default:
                super.onClick(v);
        }

    }

    @Override
    public void onResetView(Mark mark, int i) {
        this.mark = mark;
        markName.setText(mark.getName());
        if("true".equals(AppData.getCustomData("ShowMarkPirce"))) {
            markPrice.setText(String.valueOf(mark.getVersion() / 100.0));
        }else{
            markPrice.setText("");  //in case it's already displaying a "0", user changed setting, then we need to make "0" become "".
        }
        markSelectedNum.setText(Integer.toString(mark.getQt()));
    }
}
