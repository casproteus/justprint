package com.just.print.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Printer;
import com.just.print.db.expand.DaoExpand;
import com.just.print.db.expand.State;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.holder.ConfigPrinterViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

/**
 * Created by wangx on 2016/11/1.
 */
public class ConfigPrinterFragment extends BaseFragment implements
        OnClickItemListener {
    private XAdapter2<Printer> printerXAdapter = null;

    @Override
    public String toString() {
        return "打印机设置";
    }


    @XViewByID(R.id.printList)
    private ListView printerListView = null;
    /**
     * 用于存放临时修改
     */
    private Printer mModifyCache = null;

    @XViewByID(R.id.viewSwitcher)
    private ViewSwitcher modifyViewSwitch = null;

    @XViewByID(R.id.modifyPname)
    private TextView modifName = null;

    @XViewByID(R.id.modifyPip)
    private TextView modifIP = null;
    @XViewByID(R.id.modifyCheckBox)
    private CheckBox modifyCheckBox = null;

    @XClick({R.id.addPrint})
    private void addPrint(
            @XGetValueByView(fromId = R.id.pip, fromMethodName = "getText#toString#trim") String ip,
            @XGetValueByView(fromId = R.id.pname) TextView name,
            @XGetValueByView(fromId = R.id.checkBox) CheckBox checkBox,
            @XGetValueByView(fromId = R.id.printType, fromMethodName = "getCheckedRadioButtonId") int checkid
    ) {

        if (!isIP(ip)) {
            showToast("请输入正确的ip地址");
            return;
        }

        Printer printer = new Printer();
        printer.setIp(ip.toString());
        printer.setType(checkid == R.id.orderPrint ? 1 : 0);// 1: 顺序打印,0 :类别打印
        printer.setPname(name.getText().toString());
        //        if (checkBox.isChecked()) {
        //            //DaoExpand.updateAllPrintTo0(getDaoMaster().newSession().getPrinterDao());
        //            printer.setFirstPrint(1);
        //        }
        printer.setFirstPrint(checkBox.isChecked() ? 1 : 0);//是否全单打印,1 全单打印,0单独打印
        name.setText("");
        checkBox.setChecked(false);
        printer.setState(State.def);
        getDaoMaster().newSession().getPrinterDao().insert(printer);
        printer.updateAndUpgrade();
        loadPrinter();
    }

    private void loadPrinter() {
        List<Printer> list = DaoExpand.queryNotDeleteAll(getDaoMaster().newSession().getPrinterDao());
        WifiPrintService.getInstance().reInitPrintRelatedMaps(list);
        printerXAdapter.setData(list);
    }

    @XClick({R.id.modifyPrint})
    private void modifyPrint(@XGetValueByView(fromId = R.id.modifyCheckBox) CheckBox modifyCheckBox) {
        if (mModifyCache != null) {
            if (!isIP(modifIP.getText().toString())) {
                showToast("请输入正确的ip");
                return;
            }

            mModifyCache.setPname(modifName.getText().toString());
            mModifyCache.setIp(modifIP.getText().toString().trim());
            mModifyCache.setVersion(mModifyCache.getVersion() + 1);
//            if (modifyCheckBox.isChecked()) {
//                DaoExpand.updateAllPrintTo0(getDaoMaster().newSession().getPrinterDao());
//                mModifyCache.setFirstPrint(1);
//            } else
//                mModifyCache.setFirstPrint(0);
            mModifyCache.update();
            mModifyCache = null;
            printerXAdapter.notifyDataSetChanged();
        }
        modifyViewSwitch.setDisplayedChild(0);

    }

    @Override
    public void onClickItem(View view, int p) {
        switch (view.getId()) {
            case R.id.delete:
                printerXAdapter.get(p).delete();
                mModifyCache = null;
                if (modifyViewSwitch.getDisplayedChild() == 1)
                    modifyViewSwitch.setDisplayedChild(0);
                loadPrinter();
                break;
            case R.id.modify:
                mModifyCache = printerXAdapter.get(p);
                modifyCheckBox.setChecked(mModifyCache.getFirstPrint() == null ? false : mModifyCache.getFirstPrint() == 1);
                modifIP.setText(mModifyCache.getIp());
                modifName.setText(mModifyCache.getPname());
                if (modifyViewSwitch.getDisplayedChild() == 0)
                    modifyViewSwitch.setDisplayedChild(1);
                break;
            default:
        }
    }

    private boolean isIP(String ip) {
        String[] split = ip.split("\\.");
        if (split.length != 4) return false;
        try {
            for (String string : split) {
                int i = Integer.parseInt(string);
                if (i < 0 || i > 255)
                    return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_printer_fragment;
    }

    @Override
    public void onCreated(@Nullable Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        printerXAdapter = new XAdapter2<Printer>(getContext(), ConfigPrinterViewHolder.class);
        printerXAdapter.setClickItemListener(this);
        printerListView.setAdapter(printerXAdapter);
        loadPrinter();
    }


}
