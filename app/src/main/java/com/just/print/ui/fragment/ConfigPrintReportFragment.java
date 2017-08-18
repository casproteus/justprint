package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.just.print.Activate;
import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.ui.holder.ConfigPrintReportViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.Date;
import java.util.List;

public class ConfigPrintReportFragment extends BaseFragment implements OnClickItemListener {

    @XViewByID(R.id.gridView)
    FlowListView gridView;
    XAdapter2<Mark> markXAdapter;

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

        if(!Activate.currentSN.equals(mark)){
            showToast("Please input correct password!");
            return;
        }

        SaleRecordDao saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        List<SaleRecord> orders = saleRecordDao.loadAll();

        if(orders == null || orders.size() == 0){
            showToast("No report to print! The sales record has been cleaned!");
            return;
        }

        showToast("Preparing the report for print!");
        String reportStartDate = AppData.getCustomData("reportStartDate");
        if(reportStartDate == null){
            reportStartDate = AppData.getCustomData("lastsuccess");
        }
        long start = new Date(Long.valueOf(reportStartDate)).getTime();
        long now = new Date().getTime();
        boolean printedSuccessfully = false;







        if(printedSuccessfully) {
            //when printed succcesfully, clean all records, and update now as the next reportStartDate
            saleRecordDao.deleteAll();
            AppData.putCustomData("reportStartDate", String.valueOf(now));
            showToast("Report printted!");
        }else{
            showToast("Error occored during printting, please check the connection with printer!");
        }
    }


    @Override
    public void onClickItem(View view, int i) {

    }
}
