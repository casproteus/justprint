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

public class ConfigPrintReportFragment extends BaseFragment implements OnClickItemListener {

    private HashMap<String,List<String>> contentForPrintMap;

    @XViewByID(R.id.gridView)
    FlowListView gridView;
    XAdapter2<Mark> markXAdapter;

    private SaleRecordDao saleRecordDao;
    private long now;

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
        String adminPassword = AppData.getCustomData("adminPassword");
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

        String reportStartDate = AppData.getCustomData("reportStartDate");
        if(reportStartDate == null || reportStartDate.length() < 1){
            reportStartDate = AppData.getCustomData("lastsuccess");
        }

        now = new Date().getTime();

        //print code:
        String result = WifiPrintService.getInstance().exePrintReportCommand(orders, reportStartDate, String.valueOf(now));
        if("0".equals(result)) {
            findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.alertDlg).setVisibility(View.VISIBLE);
        }
    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        getActivity().finish();
    }

    @XClick({R.id.buttonOK})
    private void resetReport(){
        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        saleRecordDao.deleteAll();
        AppData.putCustomData("reportStartDate", String.valueOf(now));
        int reportIdx = 1;
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData("reportIdx"));
        }catch(Exception e){
            //report error.
        }
        AppData.putCustomData("reportIdx", String.valueOf(reportIdx + 1));

        getActivity().finish();
    }

    @Override
    public void onClickItem(View view, int i) {

    }
}
