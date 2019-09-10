package com.just.print.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Base64;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * SharedPreferences 工具类
 *
 * @author wangx 2016-04-27 重构
 **/
public class SharedPreferencesHelper {

    private static final String empty = "";

    private static final WeakHashMap<Context, Map<String, SharedPreferencesHelper>> mCache = new WeakHashMap<Context, Map<String, SharedPreferencesHelper>>(
            3);

    private static final String tag = "SharePreferenceHelper";

    /***
     * 线程安全
     **/
    public static SharedPreferencesHelper getCache(Context context, String name) {
        Map<String, SharedPreferencesHelper> hashmap = null;
        hashmap = mCache.get(context);// 根据主键 取出 主键缓存,

        if (hashmap == null) {// 如果二级缓存是否为空,
            synchronized (mCache) {// 给一级缓存加线程锁
                hashmap = mCache.get(context);// 再取出二级缓存
                if (hashmap == null) {// 判断二级缓存是否为空,在极端条件下,这个地方很有可能会被其他线程初始化
                    hashmap = new WeakHashMap<String, SharedPreferencesHelper>(
                            2);
                    mCache.put(context, hashmap);
                }

            }

        }
        SharedPreferencesHelper result = null;
        result = hashmap.get(name);
        if (result == null) {
            synchronized (hashmap) {
                result = hashmap.get(name);
                if (result == null) {
                    result = new SharedPreferencesHelper(context, name);
                    hashmap.put(name, result);
                }
            }
        }

        return result;
    }

    private static String getKey(Object key) {

        if (key == null) {
            return empty;
        } else if (key instanceof CharSequence) {

            return key.toString();

        } else if (key instanceof Class<?>) {

            return ((Class<?>) key).getName();
        } else {

            return key.getClass().getName();
        }
    }

    public synchronized static void onLowMemory() {

        mCache.clear();
    }

    final private SharedPreferences sp;// SP 杂化轨道,呵呵

    public SharedPreferencesHelper(Context context, String name) {
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public SharedPreferencesHelper(SharedPreferences sp) {
        this.sp = sp;
    }

    public SharedPreferencesHelper clear() {
        Editor editor = edit();
        editor.clear();
        commitPreferences(editor);
        return this;
    }

    private Editor edit() {
        return getSharedPreferences().edit();
    }

    public Map<String, ?> getAll() {
        return getSharedPreferences().getAll();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return getSharedPreferences().getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return getSharedPreferences().getInt(key, defValue);
    }

    public <T> T getJSON(Class<?> key, Class<T> clazz) {
        return getJSON(key.getName(), clazz);

    }

    public <T> T getJSON(Class<T> clazz) {
        return getJSON(clazz, clazz);

    }

    public <T> T getJSON(String key, Class<T> clazz) {
        try {
            return JSON.parseObject(getString((key)), clazz);
        } catch (Exception e) {
            L.w(tag, "getJSON", e);
            return null;
        }

    }

    /**
     * 测试中的方法,从xml里取出序列化的对象
     *
     * @param key
     * @throws ClassNotFoundException
     * @author wangx
     **/
    @SuppressLint("NewApi")
    public Object getObject(String key) throws ClassNotFoundException {

        String base64 = getString(key);
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(
                base64, Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(is);

            return ois.readObject();

        } catch (IOException e) {
            return null;
        }

    }

    private SharedPreferences getSharedPreferences() {

        return sp;
    }

    public String getString(String key) {

        return this.getString(key, empty);
    }

    public String getString(String key, String defValue) {

        return getSharedPreferences().getString(key, defValue);
    }

    public SharedPreferencesHelper putBoolean(String key, boolean value) {
        Editor editor = edit();
        editor.putBoolean(key, value);

        commitPreferences(editor);

        return this;
    }

    public SharedPreferencesHelper putInt(String key, int value) {

        Editor editor = edit();
        editor.putInt(key, value);

        commitPreferences(editor);
        return this;

    }

    public SharedPreferencesHelper putJSON(Class<?> key, Object value) {

        putJSON(key.getName(), value);
        return this;
    }

    public SharedPreferencesHelper putJSON(Object value) {

        putJSON(value.getClass().getName(), value);
        return this;
    }

    public SharedPreferencesHelper putJSON(String key, Object value) {
        Editor editor = edit();
        editor.putString(
                getKey(key),
                value instanceof CharSequence ? value.toString() : JSON
                        .toJSONString(value));

        commitPreferences(editor);
        return this;
    }

    public SharedPreferencesHelper putLon(String key, long value) {
        Editor editor = edit();
        editor.putLong(key, value);

        commitPreferences(editor);
        return this;
    }

    /**
     * 测试中的方法,保存序列化的对象到xml里
     *
     * @param key
     * @author wangx
     **/
    @SuppressLint("NewApi")
    public void putObject(String key, Serializable serializable)
            throws IOException {

        ByteArrayOutputStream bots = new ByteArrayOutputStream();
        ObjectOutputStream opt = new ObjectOutputStream(bots);
        opt.writeObject(serializable);
        opt.flush();
        opt.close();
        opt.reset();

        String base64 = Base64.encodeToString(bots.toByteArray(),
                Base64.DEFAULT);
        putString(key, base64);
        bots = null;

        opt = null;
        base64 = null;

    }

    public SharedPreferencesHelper putString(String key, String value) {

        Editor editor = edit();
        editor.putString(key, value);

        commitPreferences(editor);
        return this;
    }

    public SharedPreferencesHelper registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(
                listener);
        return this;
    }

    public SharedPreferencesHelper remove(String key) {
        Editor editor = edit();
        editor.remove(key);

        commitPreferences(editor);
        return this;

    }

    public SharedPreferencesHelper unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                listener);
        return this;
    }

    @SuppressLint("NewApi")
    private static void commitPreferences(Editor editor) {
        if (isGingerbreadOrLater())
            editor.apply();
        else
            editor.commit();
    }

    @SuppressLint("NewApi")
    private static boolean isGingerbreadOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
    }
}
