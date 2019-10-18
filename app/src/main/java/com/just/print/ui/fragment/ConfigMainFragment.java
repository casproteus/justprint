package com.just.print.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.SaleRecord;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.activity.ConfigActivity;
import com.just.print.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.Date;
import java.util.List;


public class ConfigMainFragment extends BaseFragment {

    private String reportContent;

    @XViewByID(R.id.password)
    EditText password;

    @Override
    protected int getLayoutId() {
        return R.layout.config_main_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        if(!"true".equalsIgnoreCase(AppData.getCustomData(AppData.TableSelectable))) {
            findViewById(R.id.configTableManager).setVisibility(View.GONE);
        }
    }

    @XClick({R.id.verifyPassword})
    private void onConfirmUserName(View view) {
        String inputContent = this.password.getText().toString().trim();
        if (StringUtils.isEmpty(inputContent) || inputContent.length() < 2) {
            showToast("Please input the right password");
        } else {
            String userPassword = AppData.getCustomData(AppData.userPassword);
            if(userPassword == null || userPassword.length() == 0){
                userPassword = "88882288";
            }

            if(!userPassword.equals(inputContent)){
                if(AppData.getCustomData(AppData.adminPassword).equals(inputContent)){
                    List<SaleRecord> orders = Applic.app.getDaoMaster().newSession().getSaleRecordDao().loadAll();
                    reportContent = WifiPrintService.getInstance().exePrintReportCommand(orders, String.valueOf(new Date().getTime()));
                    findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
                    findViewById(R.id.alertDlg).setVisibility(View.VISIBLE);
                }else {
                    showToast("Please input the right password");
                }
                return;
            }

            findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.configList).setVisibility(View.VISIBLE);
        }
    }

    @XClick({R.id.configMenuManager, R.id.configPrintManager, R.id.configTagManager, R.id.configTableManager,
            R.id.configUserManager, R.id.configPrintReport, R.id.uploadDB, R.id.downloadDB, R.id.uploadConfig, R.id.downloadConfig, R.id.reactivate})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.configMenuManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigMenuFragment.class);
                break;
            case R.id.configUserManager:
                // getEventBus().post(ConfigActivity.CHANGE_PAGE, Config.class);
                break;
            case R.id.configTagManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigMarkFragment.class);
                break;
            case R.id.configTableManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigTableFragment.class);
                break;
            case R.id.configPrintManager:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigPrinterFragment.class);
                break;
            case R.id.configPrintReport:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ConfigPrintReportFragment.class);
                break;
            case R.id.uploadDB:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, UploadingConfirmFragment.class);
                break;
            case R.id.downloadDB:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, DownloadingConfirmFragment.class);
                break;
            case R.id.uploadConfig:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, UploadingConfigConfirmFragment.class);
                break;
            case R.id.downloadConfig:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, DownloadingConfigConfirmFragment.class);
                break;
            case R.id.reactivate:
                getEventBus().post(ConfigActivity.CHANGE_PAGE, ReActivateFragment.class);
                break;
        }
    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);
        //check if need to send email
        try{
            int reportIdx = 1;
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
            AppData.notifyCheck(reportIdx, reportContent, false);
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        startActivity(new Intent(getContext(), ConfigActivity.class));
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        int reportIdx = 1;
        //check if need to send email
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        if(!AppData.notifyCheck(reportIdx, reportContent, true)){
            return;
        }

        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        Applic.app.getDaoMaster().newSession().getSaleRecordDao().deleteAll();
        AppData.putCustomData(AppData.reportStartDate, String.valueOf(new Date().getTime()));
        AppData.putCustomData("kitchenBillIdx", "1");        //reset kitchenbillIndex

        AppData.putCustomData(AppData.reportIdx, String.valueOf(reportIdx + 1));

        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);

        startActivity(new Intent(getContext(), ConfigActivity.class));
    }
}
