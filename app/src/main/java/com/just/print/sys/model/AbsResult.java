package com.just.print.sys.model;

/**
 * Created by wangx on 2016/11/4.
 */
abstract
public class AbsResult extends AbsModel {
    {
        setType(2);
    }

    @Override
    final public String getService() {
        return null;
    }

    @Override
    final public int getInnerType() {
        return 0;
    }
}
