package com.just.print.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Printer;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.holder.ConfigPrinterViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

public class ConfigPrinterFragment extends BaseFragment implements
        OnClickItemListener {
    private XAdapter2<Printer> printerXAdapter = null;

    @Override
    public String toString() {
        return "Printer Settings";
    }

    @XViewByID(R.id.password)
    EditText password;

    @XViewByID(R.id.printList)
    private ListView printerListView = null;
    /**
     * for storing tempral modification
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

    @XViewByID(R.id.modifyOrderPrint)
    private CheckBox modifyOrderPrint = null;

    @XViewByID(R.id.modifyClassPrint)
    private CheckBox modifyClassPrint = null;
    @XViewByID(R.id.modifyNote)
    private TextView modifyNote = null;

    @XClick({R.id.verifyPassword})
    private void onVerifyPassword(View view) {
        String inputContent = this.password.getText().toString().trim();
        String adminPassword = AppData.getCustomData("adminPassword");
        if(adminPassword == null || adminPassword.length() == 0){
            adminPassword = AppData.getLicense();
        }

        if(!adminPassword.equals(inputContent)){
            showToast("Please input the right password");
            return;
        }

        findViewById(R.id.confirmPassword).setVisibility(View.INVISIBLE);
        findViewById(R.id.storeName).setVisibility(View.VISIBLE);
    }

    @XClick({R.id.addPrint})
    private void addPrint(
            @XGetValueByView(fromId = R.id.pip, fromMethodName = "getText#toString#trim") String ip,
            @XGetValueByView(fromId = R.id.pname) TextView name,
            @XGetValueByView(fromId = R.id.checkBox) CheckBox checkBox,
            //@XGetValueByView(fromId = R.id.printType, fromMethodName = "getCheckedRadioButtonId") int checkid,
            @XGetValueByView(fromId = R.id.orderPrint) CheckBox orderPrint,
            @XGetValueByView(fromId = R.id.classPrint) CheckBox classPrint,
            @XGetValueByView(fromId = R.id.note) TextView note) {

        if (!isIP(ip)) {
            showToast("Please input correct IP address.");
            return;
        }
        int type = 0;
        if(orderPrint.isChecked() && !classPrint.isChecked()){
            type = 1;
        }else if(!orderPrint.isChecked() && classPrint.isChecked()){
            type = 0;
        }else if(!orderPrint.isChecked() && !classPrint.isChecked()){
            type = 2;
        }else if(orderPrint.isChecked() && classPrint.isChecked()){
            type = 3;
        }
        AppData.createPrinter(ip.toString(),
                name.getText().toString(),
                checkBox.isChecked() ? 1 : 0,
                type,//checkid == R.id.orderPrint ? 1 : 0,
                note.getText().toString());

        name.setText("");
        checkBox.setChecked(false);
        AppData.updataeLastModifyTime(null);
        loadPrinter();
    }

    private void loadPrinter() {
        List<Printer> list = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getPrinterDao());
        WifiPrintService.getInstance().reInitPrintRelatedMaps();
        printerXAdapter.setData(list);
    }

    @XClick({R.id.modifyPrint})
    private void modifyPrint(
            @XGetValueByView(fromId = R.id.modifyCheckBox) CheckBox modifyCheckBox,
            @XGetValueByView(fromId = R.id.modifyOrderPrint) CheckBox modifyOrderPrint,
            @XGetValueByView(fromId = R.id.modifyClassPrint) CheckBox modifyClassPrint) {
        if (mModifyCache != null) {
            if (!isIP(modifIP.getText().toString())) {
                showToast("Please input correct ip");
                return;
            }

            mModifyCache.setPname(modifName.getText().toString());
            mModifyCache.setIp(modifIP.getText().toString().trim());
            mModifyCache.setVersion(mModifyCache.getVersion() + 1);
            if(modifyOrderPrint.isChecked() && !modifyClassPrint.isChecked()){
                mModifyCache.setType(1);// 1: 顺序打印,
            }else if(!modifyOrderPrint.isChecked() && modifyClassPrint.isChecked()){
                mModifyCache.setType(0);//0 :类别打印
            }else if(!modifyOrderPrint.isChecked() && !modifyClassPrint.isChecked()){
                mModifyCache.setType(2);// tbd
            }else if(modifyOrderPrint.isChecked() && modifyClassPrint.isChecked()){
                mModifyCache.setType(3);//按类别排序的全打
            }
            //DaoExpand.updateAllPrintTo0(getDaoMaster().newSession().getPrinterDao());
            mModifyCache.setFirstPrint(modifyCheckBox.isChecked() ? 1 : 0);
            mModifyCache.setNote(modifyNote.getText().toString());
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
                if(mModifyCache.getType() == 0){    //by category
                    modifyOrderPrint.setChecked(false);
                    modifyClassPrint.setChecked(true);
                }else if(mModifyCache.getType() == 1){                                  //by order time
                    modifyOrderPrint.setChecked(true);
                    modifyClassPrint.setChecked(false);
                }else if(mModifyCache.getType() == 2){
                    modifyOrderPrint.setChecked(false);
                    modifyClassPrint.setChecked(false);
                }else if(mModifyCache.getType() == 3){
                    modifyOrderPrint.setChecked(true);
                    modifyClassPrint.setChecked(true);
                }
                modifyNote.setText(mModifyCache.getNote());

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
