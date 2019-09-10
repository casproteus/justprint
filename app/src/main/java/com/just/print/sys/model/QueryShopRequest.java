package com.just.print.sys.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.just.print.sys.server.DefaultService;

/**
 * Created by wangx on 2016/11/4.
 */
public class QueryShopRequest extends AbsRequest {

    private String shopName;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    @Override
    public String getService() {
        return DefaultService.class.getName();
    }

    @Override
    @JSONField(serialize = false)
    public int getInnerType() {
        return 1;
    }
}
