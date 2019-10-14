package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.db.expand.DaoExpand;
import com.just.print.ui.holder.ConfigTableViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigTableFragment extends BaseFragment implements OnClickItemListener {

    @XViewByID(R.id.gridView)
    FlowListView gridView;
    XAdapter2<Mark> markXAdapter;

    /**
     * for storing tempral modification
     */
    private Mark onEditMark = null;

    @XViewByID(R.id.name)
    private TextView name = null;

    @XViewByID(R.id.dspIdx)
    private TextView dspIdx = null;

    @XViewByID(R.id.viewSwitcher)
    private ViewSwitcher modifyViewSwitch = null;

    @Override
    protected int getLayoutId() {
        return R.layout.config_table_fragment;
    }

    int onEditIdx = 0;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        markXAdapter = new XAdapter2<Mark>(getContext(), ConfigTableViewHolder.class);
        gridView.setAdapter(markXAdapter);
        markXAdapter.setClickItemListener(this);
        loadTables();
    }

    private void loadTables() {
        OrderIdentifierFragment.loadTables();
        markXAdapter.setData(OrderIdentifierFragment.tables);
        markXAdapter.notifyDataSetChanged();
    }

    @XClick({R.id.addTable})
    private void addMark(@XGetValueByView(fromId = R.id.etTable) String mark,
                         @XGetValueByView(fromId = R.id.dspIdxTable) String dspIdx) {

        Mark mark1 = new Mark();        //currently we are using Mark to save table( when deleted flag is set, then it's table.
        mark1.setName(mark);

        try{
            mark1.setState((int)(Float.valueOf(dspIdx) * 100));
        }catch(NumberFormatException e){
            mark1.setState(0);
        }
        mark1.setVersion(-1l);
        Applic.app.getDaoMaster().newSession().getMarkDao().insertOrReplace(mark1);
        mark1.updateAndUpgrade();
        AppData.updataeLastModifyTime(null);
        loadTables();
    }


    @Override
    public void onClickItem(View view, int i) {
        onEditIdx = i;
        final Mark mark = markXAdapter.getItem(i);
        onEditMark = mark;

        name.setText(mark.getName());
        dspIdx.setText(String.valueOf(((float) mark.getState()) / 100.00));

        if (modifyViewSwitch.getDisplayedChild() == 0){
            modifyViewSwitch.setDisplayedChild(1);
        }
    }

    @XClick({R.id.modifyLable})
    private void confirmChange(){
        if (onEditMark != null) {
//            if (!isIP(modifIP.getText().toString())) {
//                showToast("Please input correct ip");
//                return;
//            }

            onEditMark.setName(name.getText().toString());

            try{
                onEditMark.setState((int)(Float.valueOf(dspIdx.getText().toString()) * 100));
            }catch(NumberFormatException e){
                //do nothing.
            }

            onEditMark.update();
            onEditMark = null;
            markXAdapter.notifyDataSetChanged();
            AppData.updataeLastModifyTime(null);
        }
        modifyViewSwitch.setDisplayedChild(0);
    }

    @XClick({R.id.delete})
    private void delete(){
        final Mark mark = markXAdapter.getItem(onEditIdx);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to delete \"" + mark.getName() + "\"?");
        builder.setTitle("Notice").setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mark.delete();
                loadTables();

                modifyViewSwitch.setDisplayedChild(0);
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
