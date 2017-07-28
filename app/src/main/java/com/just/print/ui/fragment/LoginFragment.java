package com.just.print.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;
import com.just.print.sys.model.QueryShopRequest;
import com.just.print.sys.model.QueryShopResult;
import com.just.print.ui.activity.ConfigActivity;
import com.just.print.ui.activity.OrderActivity;
import com.just.print.util.Base64Util;
import com.just.print.util.StringUtils;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

/**
 * Created by wangx on 2016/11/2.
 */
public class LoginFragment extends BaseFragment implements UDPService.UDPCallback {
    @Override
    protected int getLayoutId() {
        return R.layout.login_fragment;
    }

    @XViewByID(R.id.userName)
    EditText userName;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        String UserName = AppData.getUserName(getContext());
        String shopName = AppData.getShopName(getContext());
        if (!StringUtils.isEmpty(UserName)) {
            userName.setText(UserName);
            userName.setEnabled(false);
            findViewById(R.id.confirmUserName).setVisibility(View.GONE);
            findViewById(R.id.shopShow).setVisibility(View.VISIBLE);
        }
        if (!StringUtils.isEmpty(shopName)) {
            //showToast(shopName);
            AppData.createShopDB(getContext(), shopName);
            startActivity(new Intent(getContext(), OrderActivity.class));
            getActivity().finish();
        }

    }


    @XClick(R.id.queryShop)
    private void queryShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
        if (StringUtils.isEmpty(shopName)) {
            showToast("请输入店铺名称");
            return;
        }
        if (!AppData.existShop(getContext(), shopName)) {
            QueryShopRequest request = new QueryShopRequest();
            request.setShopName(shopName);
            Applic.getApp().mUDPService.sendRequest(request, 1, this);
        }
    }


    @Override
    public boolean onCallback(final boolean status, final int requestCode, AbsModel result) {
        switch (requestCode) {
            case 1:
                final QueryShopResult result2 = result == null ? null : (QueryShopResult) result;
                if (status && result2 != null && result2.isExists()) {

                    byte[] datas = Base64Util.decode(result2.getFileBase64());
                    AppData.writeDBByte(getContext(), datas, result2.getShopName());
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
                            showToast("店铺不存在,或者网络中没有该店铺的手机");
                        }
                    }
                });
                break;
        }
        return true;
    }


    final static String tag = "login";

    @XClick({R.id.createShop})
    private void createShop(@XGetValueByView(fromId = R.id.shopName) String shopName) {
        if (StringUtils.isEmpty(shopName)) {
            showToast("请输入店铺名称");
            return;
        }
        AppData.saveShopName(getContext(), shopName);
        AppData.createShopDB(getContext(), shopName);
        startActivity(new Intent(getContext(), OrderActivity.class));
        getActivity().finish();
    }

    @XClick({R.id.confirmUserName})
    private void onConfirmUserName(View view) {
        String userName = this.userName.getText().toString().trim();
        if (StringUtils.isEmpty(userName) || userName.length() < 2) {
            showToast("请输入姓名");
        } else {
            findViewById(R.id.shopShow).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            AppData.saveUserName(getContext(), userName);
            this.userName.setEnabled(false);
        }
    }
}
