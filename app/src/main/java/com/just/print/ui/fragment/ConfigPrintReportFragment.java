package com.just.print.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.just.print.Activate;
import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Printer;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.dao.SaleRecordDao;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.activity.OrderActivity;
import com.just.print.ui.holder.ConfigPrintReportViewHolder;
import com.just.print.util.L;
import com.just.print.util.ToastUtil;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigPrintReportFragment extends BaseFragment implements OnClickItemListener {

    private HashMap<String,List<String>> contentForPrintMap;

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
        //maybe we should hide the functions into a "more..." button.
        String adminPassword = AppData.getCustomData("adminPassword");
        if(adminPassword != null && !adminPassword.equals(mark)){
            showToast("Please input correct password!");
            return;
        }

        SaleRecordDao saleRecordDao = Applic.app.getDaoMaster().newSession().getSaleRecordDao();
        List<SaleRecord> orders = saleRecordDao.loadAll();

        if(orders == null || orders.size() == 0){
            showToast("No report to print! The sales record has been cleaned!");
            return;
        }

        String reportStartDate = AppData.getCustomData("reportStartDate");
        if(reportStartDate == null || reportStartDate.length() < 1){
            reportStartDate = AppData.getCustomData("lastsuccess");
        }

        long now = new Date().getTime();

        //print code:
        String result = WifiPrintService.getInstance().exePrintReportCommand(orders, reportStartDate, String.valueOf(now));
        if("0".equals(result)) {
            //when printed succcesfully, clean all records, and update now as the next reportStartDate
            saleRecordDao.deleteAll();
            AppData.putCustomData("reportStartDate", String.valueOf(now));
            showToast("Report printted!");

            startActivity(new Intent(getContext(), OrderActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onClickItem(View view, int i) {

    }
}
