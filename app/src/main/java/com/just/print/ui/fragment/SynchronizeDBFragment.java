package com.just.print.ui.fragment;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseFragment;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.util.DatabaseUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

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
        String lastSyncDate = AppData.getLastModifyTime();
        if(lastSyncDate == null || lastSyncDate.length() == 0){
            lastSyncDate = "1";
        }
        lastModified.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(lastSyncDate)));
    }

    @XClick({R.id.buttonOK})
    private void onButtonOK(){
        DatabaseUtil.syncDbOntoServer(store_name.getText().toString(), password.getText().toString());
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        getActivity().finish();
    }

}
