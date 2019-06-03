package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.db.expand.DaoExpand;
import com.just.print.db.expand.State;
import com.just.print.ui.holder.ConfigMarkViewHolder;
import com.just.print.util.L;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigMarkFragment extends BaseFragment implements OnClickItemListener {

    @XViewByID(R.id.gridView)
    FlowListView gridView;
    XAdapter2<Mark> markXAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.config_mark_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        markXAdapter = new XAdapter2<Mark>(getContext(), ConfigMarkViewHolder.class);
        gridView.setAdapter(markXAdapter);
        markXAdapter.setClickItemListener(this);
        loadMark();
    }

    private void loadMark() {
        List<Mark> list = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getMarkDao());
        Collections.sort(list, new Comparator<Mark>() {
            @Override
            public int compare(Mark mark, Mark t1) {
                return mark.getState() - t1.getState();
            }
        });
        markXAdapter.setData(list);
        markXAdapter.notifyDataSetChanged();
    }

    @XClick({R.id.addMark})
    private void addMark(@XGetValueByView(fromId = R.id.etMark) String mark,
                         @XGetValueByView(fromId = R.id.dspIdxMark) String dspIdx,
                         @XGetValueByView(fromId = R.id.priceMark) String price) {

        Mark mark1 = new Mark();

        mark1.setName(mark);

        try{
            mark1.setState((int)(Float.valueOf(dspIdx) * 100));
        }catch(NumberFormatException e){
            //do nothing.
        }

        try{
            mark1.setVersion((long) (Float.valueOf(price) * 100));
        }catch(NumberFormatException e){
            //do nothing.
        }

        Applic.app.getDaoMaster().newSession().getMarkDao().insertOrReplace(mark1);
        mark1.updateAndUpgrade();
        AppData.updataeLastModifyTime(null);
        loadMark();
    }


    @Override
    public void onClickItem(View view, int i) {
        final
        Mark mark = markXAdapter.getItem(i);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to delete \"" + mark.getName() + "\"?");
        builder.setTitle("Notice").setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                mark.logicDelete();
//                loadMark();
            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
