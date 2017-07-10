package com.just.print.util;

import android.util.Base64;

/**
 * Created by wangx on 2016/11/14.
 */
final public class Base64Util {

    public static String encodeToString(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_PADDING);
    }

    public static byte[] decode(String str) {
        return Base64.decode(str, Base64.NO_PADDING);
    }
}
