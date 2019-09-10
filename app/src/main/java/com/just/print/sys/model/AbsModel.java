package com.just.print.sys.model;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangx on 2016/10/31.
 */
abstract public class AbsModel implements IModel {
    public static Map<String, Class<? extends AbsModel>> CHILD_CLASS = new HashMap<>();
    private int requestID;
    private int type;
    private int requestCode;
    public String deviceID;

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public <T> T typeCoercion() {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * 添加子类
     *
     * @param clz
     */
    public static void addChildClass(Class<? extends AbsModel> clz) {
        CHILD_CLASS.put(clz.getName(), clz);
    }

    /**
     * 获得子类
     *
     * @param cls
     * @return
     */
    public static Class<? extends AbsModel> getChildClassOrLoad(String cls) {
        Class<? extends AbsModel> result = CHILD_CLASS.get(cls);
        if (result == null) {
            try {
                result = (Class<? extends AbsModel>) Class.forName(cls);
                CHILD_CLASS.put(cls, result);
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }

    @JSONField(name = "CLASS_NAME")
    final public String getClassName() {
        return getClass().getName();
    }

    @Override
    final public String toString() {
        return JSON.toJSONString(this);
    }

    @JSONField(serialize = false)
    final public String toByte64() {
        String res;
        res = new String(Base64.encode(toString().getBytes(), Base64.NO_PADDING));
        return res;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
