package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.holder.ConfigPrintReportViewHolder;
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

import static com.just.print.util.ToastUtil.showToast;

public class ConfigPrintReportFragment extends BaseFragment implements OnClickItemListener {

    private HashMap<String,List<String>> contentForPrintMap;

    @XViewByID(R.id.gridView)
    FlowListView gridView;
    XAdapter2<Mark> markXAdapter;

    private SaleRecordDao saleRecordDao;
    private long now;
    private String reportContent;

    @Override
    protected int getLayoutId() {
        return R.layout.config_printreport_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        markXAdapter = new XAdapter2<Mark>(getContext(), ConfigPrintReportViewHolder.class);
        gridView.setAdapter(markXAdapter);
        markXAdapter.setClickItemListener(this);
    }

    @XClick({R.id.verifyPassword})
    private void verifyPassword(@XGetValueByView(fromId = R.id.etMark) String mark) {
        //maybe we should hide the functions into a "more..." button.
        String adminPassword = AppData.getCustomData(AppData.adminPassword);
        if(adminPassword != null && !adminPassword.equals(mark)){
            showToast("Please input correct password!");
            return;
        }

        saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        List<SaleRecord> orders = saleRecordDao.loadAll();

        if(orders == null || orders.size() == 0){
            showToast("No report to print! The sales record has been cleaned!");
            return;
        }
        //remem the now for later use.
        now = new Date().getTime();
        //print code:
        reportContent = WifiPrintService.getInstance().exePrintReportCommand(orders, String.valueOf(now));
        findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setVisibility(View.VISIBLE);
    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        int reportIdx = 1;
        //check if need to send email
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //It's OK if reportIdx is null
        }
        if(AppData.notifyCheck(reportIdx, reportContent, false)) {
            getActivity().finish();
        }
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        int reportIdx = 1;
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData(AppData.reportIdx));
        }catch(Exception e){
            //report error.
        }
        if(!AppData.notifyCheck(reportIdx, reportContent, true)){
            return;
        }
        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        saleRecordDao.deleteAll();
        AppData.putCustomData(AppData.reportStartDate, String.valueOf(now));
        AppData.putCustomData(AppData.kitchenBillIdx, "1");        //reset kitchenbillIndex

        AppData.putCustomData(AppData.reportIdx, String.valueOf(reportIdx + 1));
        getActivity().finish();
    }

    @Override
    public void onClickItem(View view, int i) {

    }
}
