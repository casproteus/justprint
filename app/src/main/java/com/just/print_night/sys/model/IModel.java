package com.just.print_night.sys.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by wangx on 2016/11/4.
 */
public interface IModel {
    @JSONField(serialize = false)
    String getService();

    @JSONField(serialize = false)
    int getInnerType();

}
