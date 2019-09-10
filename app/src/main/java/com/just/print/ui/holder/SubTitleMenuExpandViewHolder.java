package com.just.print.ui.holder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Menu;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.stupid.method.adapter.expand.XExpadnViewHolder;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

/**
 * Created by Administrator on 2016/11/17.
 */

public class SubTitleMenuExpandViewHolder extends XExpadnViewHolder<Menu> {

    @XViewByID(R.id.code)
    TextView code;
    @XViewByID(R.id.title)
    TextView title;
    @XViewByID(R.id.button)
    Button button;

    @Override
    protected void onNodeIsChild(boolean b, int i) {

    }

    @Override
    protected void onNodeIsParent(boolean b, int i) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.title_subtitle;
    }

    @Override
    public void onCreate(Context context) {
        new StupidReflect(this, getView()).init();
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        button.setTextColor(Color.rgb(000, 145, 213));
        super.onClick(v);
    }
    @Override
    public void onResetView(Menu menu, int i) {
        code.setText(menu.getID());
        title.setText(menu.getMname());
        button.setTextColor(Color.rgb(000, 000, 000));
        List<SelectionDetail> selections =  CustomerSelection.getInstance().getSelectedDishes();
        if(selections != null) {
            for (SelectionDetail selectionDetail : selections) {
                if(selectionDetail.getDish().getID().equals(menu.getID())){
                    button.setTextColor(Color.rgb(000, 145, 213));
                    button.setTypeface(null, Typeface.BOLD);
                }
            }
        }
    }
}
