package com.just.print_night.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.db.dao.SaleRecordDao;
import com.just.print_night.util.DatabaseUtil;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;

import java.util.HashMap;
import java.util.List;

public class UploadingConfirmFragment extends BaseFragment{

    private HashMap<String,List<String>> contentForPrintMap;

    private SaleRecordDao saleRecordDao;
    private long now;

    @Override
    protected int getLayoutId() {
        return R.layout.uploading_confirm_fragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        String lastSyncDate = AppData.getLastModifyTime();
        if(lastSyncDate == null || lastSyncDate.length() == 0){
            lastSyncDate = "1";
        }
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void onButtonOK(){
        DatabaseUtil.syncDbOntoServer(AppData.getLicense(), AppData.getShopName(),true);
        getActivity().finish();
    }

    @XClick({R.id.buttonCancel})
    private void onButtonCancel(){
        getActivity().finish();
    }

}
