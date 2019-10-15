package com.just.print.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.BaseFragment;
import com.just.print.util.DatabaseUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.HashMap;
import java.util.List;

import java.text.SimpleDateFormat;

public class DownloadingConfirmFragment extends BaseFragment{

    private HashMap<String,List<String>> contentForPrintMap;

    @XViewByID(R.id.store_name)
    EditText store_name;
    @XViewByID(R.id.password)
    EditText password;
    @XViewByID(R.id.lastModified)
    TextView lastModified;

    @Override
    protected int getLayoutId() {
        return R.layout.downloading_confirm_fragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        if(!"true".equals(AppData.getCustomData(AppData.AllowDownloadFromeOthers))) {
            findViewById(R.id.notice2).setVisibility(View.GONE);
            findViewById(R.id.inputArea).setVisibility(View.GONE);
        }

        store_name.setText(AppData.getShopName());
        //password.setText(AppData.getLicense());
        String lastSyncDate = AppData.getLastModifyTime();
        if(lastSyncDate == null || lastSyncDate.length() == 0){
            lastSyncDate = "1";
        }
        lastModified.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(lastSyncDate)));
    }

    @XClick({R.id.btnConfirmDownloadOK})
    private void onButtonOK(){
        String license = password.getText().toString();
        if(license.length() < 6){
            license = AppData.getLicense();
        }
        DatabaseUtil.syncDbOntoServer(license, store_name.getText().toString(),false);
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        getActivity().finish();
    }

}
