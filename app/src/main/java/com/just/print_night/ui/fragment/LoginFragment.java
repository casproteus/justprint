package com.just.print_night.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.net.UDPService;
import com.just.print_night.sys.model.AbsModel;
import com.just.print_night.sys.model.QueryShopResult;
import com.just.print_night.ui.activity.OrderActivity;
import com.just.print_night.util.Base64Util;
import com.just.print_night.util.DatabaseUtil;
import com.just.print_night.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;


/**
 * the interface is for inputting shop name and user name.
 */
public class LoginFragment extends BaseFragment implements UDPService.UDPCallback {
    private static String registeredName;
    public static String getUserName() {
        return registeredName;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.login_fragment;
    }

    @XViewByID(R.id.userName)
    EditText userName;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        registeredName = AppData.getUserName(getContext());
        String shopName = AppData.getShopName(getContext());
        if (!StringUtils.isEmpty(registeredName)) {
            userName.setText(registeredName);
            userName.setEnabled(false);
            findViewById(R.id.confirmUserName).setVisibility(View.GONE);
            findViewById(R.id.storeName).setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(shopName)) {
            //showToast(shopName);
            AppData.createShopDB(getContext(), shopName);
            startActivity(new Intent(getContext(), OrderActivity.class));
            getActivity().finish();
        }

    }


//    @XClick(R.id.queryShop)
//    private void queryShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
//        if (StringUtils.isEmpty(shopName)) {
//            showToast("Please input the name for the store.");
//            return;
//        }
//        if (!AppData.existShop(getContext(), shopName)) {
//            QueryShopRequest request = new QueryShopRequest();
//            request.setShopName(shopName);
//            Applic.app.mUDPService.sendRequest(request, 1, this);
//        }
//    }


    @Override
    public boolean onCallback(final boolean status, final int requestCode, AbsModel result) {
        switch (requestCode) {
            case 1:
                final QueryShopResult result2 = result == null ? null : (QueryShopResult) result;
                if (status && result2 != null && result2.isExists()) {

                    byte[] datas = Base64Util.decode(result2.getFileBase64());
                    DatabaseUtil.writeDBByte(datas);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status && result2 != null && result2.isExists()) {
                            AppData.saveShopName(getContext(), result2.getShopName());
                            AppData.createShopDB(getContext(), result2.getShopName());
                            //showToast("店铺存在");
                            startActivity(new Intent(getContext(), OrderActivity.class));
                            getActivity().finish();
                        } else {
//                            String string = ((TextView) findViewById(R.id.shopName)).getText().toString();
//                            AppData.saveShopName(getContext(), string);
//                            AppData.createShopDB(getContext(), string);
                            findViewById(R.id.createShop).setVisibility(View.VISIBLE);
                            showToast("store not existing, or no device belongs to this store in network was found");
                        }
                    }
                });
                break;
        }
        return true;
    }

    @XClick({R.id.confirmUserName})
    private void onConfirmUserName(View view) {
        String userName = this.userName.getText().toString().trim();
        if (StringUtils.isEmpty(userName) || userName.length() < 2) {
            showToast("Please input your name");
        } else {
            findViewById(R.id.storeName).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);

            int p = userName.indexOf("-p:");
            if(p > -1){// means theres parameters
                String password = userName.substring(p+3).trim();
                AppData.putCustomData("userPassword", password);
                userName = userName.substring(0, p).trim();
            }

            AppData.saveUserName(getContext(), userName);
            this.userName.setEnabled(false);
        }
    }

    @XClick({R.id.createShop})
    private void createShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
        if (StringUtils.isEmpty(shopName)) {
            showToast("Please input the name of the store.");
            return;
        }

        int p = shopName.indexOf("-p:");
        if(p > -1){// means theres parameters
            String password = shopName.substring(p+3).trim();
            AppData.putCustomData("adminPassword", password);
            shopName = shopName.substring(0, p).trim();
        }

        AppData.saveShopName(getContext(), shopName);
        AppData.createShopDB(getContext(), shopName);

        AppData.createPrinter("192.168.8.10","10",1,1, "");
        AppData.createPrinter("192.168.8.20","20",1,1, "");
        AppData.createPrinter("192.168.8.30","30",1,1, "");
        AppData.createPrinter("192.168.8.40","40",1,1, "");

        //initialize the days left and the stated days on cloud.
        new AppData().start();

        startActivity(new Intent(getContext(), OrderActivity.class));
        getActivity().finish();
    }

}
