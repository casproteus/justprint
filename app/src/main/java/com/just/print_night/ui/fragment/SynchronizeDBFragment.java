package com.just.print_night.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.db.bean.Mark;
import com.just.print_night.db.bean.SaleRecord;
import com.just.print_night.db.dao.SaleRecordDao;
import com.just.print_night.sys.server.WifiPrintService;
import com.just.print_night.ui.activity.ConfigActivity;
import com.just.print_night.ui.activity.OrderActivity;
import com.just.print_night.ui.holder.ConfigPrintReportViewHolder;
import com.just.print_night.util.DatabaseUtil;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SynchronizeDBFragment extends BaseFragment{

    private HashMap<String,List<String>> contentForPrintMap;

    @XViewByID(R.id.store_name)
    EditText store_name;
    @XViewByID(R.id.password)
    EditText password;
    @XViewByID(R.id.lastModified)
    TextView lastModified;

    private SaleRecordDao saleRecordDao;
    private long now;

    @Override
    protected int getLayoutId() {
        return R.layout.synchronize_db_fragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        store_name.setText(AppData.getShopName());
        //password.setText(AppData.getLicense());
        lastModified.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(AppData.getLastSyncDate())));
    }

    @XClick({R.id.buttonOK})
    private void onButtonOK(){
        DatabaseUtil.syncDbOntoServer(store_name.getText().toString(), password.getText().toString());
        startActivity(new Intent(getContext(), ConfigActivity.class));
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        startActivity(new Intent(getContext(), ConfigActivity.class));
        getActivity().finish();
    }

}
