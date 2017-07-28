package com.just.print.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * @author wangx
 **/
public class ToastUtil {
    volatile static WeakReference<Toast> refToast = null;
    volatile static WeakReference<Handler> refHandler = null;

    private static Toast getToast() {
        return refToast == null ? null : refToast.get();
    }

    public static void showToast(Context context, CharSequence text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, CharSequence text, int duration) {
        showToast(context, text, duration, Gravity.BOTTOM);
    }

    public static void showToast(Context context, CharSequence text, int duration, int gravity) {
        showToast(context, text, null, duration, gravity);
    }

    //---------------------------------------------------------
    public static void showToast(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, int resId, int duration) {
        showToast(context, context.getResources().getString(resId), duration,
                Gravity.BOTTOM);
    }

    public static void showToast(Context context, View view) {

        showToast(context, view, Toast.LENGTH_SHORT, Gravity.BOTTOM);
    }

    public static void showToast(Context context, View view, int duration) {

        showToast(context, view, duration, Gravity.BOTTOM);
    }

    public static void showToast(Context context, View view, int duration, int gravity) {
        showToast(context, null, view, duration, gravity);
    }

    private static void showToast(Context context, CharSequence text, View view, int duration, int gravity) {
        //如果处于UI线程里,则直接显示,否则 放到ＵＩ线程里显示
        if (Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId()) {
            Toast toast = getToast();
            if (toast != null)
                toast.cancel();

            toast = text != null ? Toast.makeText(context, text, duration) : new Toast(context);
            toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
            toast.show();
            refToast = new WeakReference<Toast>(toast);
        } else
            showToastOnUiThread(context, text, null, duration, gravity);

        /*
        if (Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId()) {
            Toast toast = getToast();
            if (toast != null)
                toast.cancel();
            toast = new Toast(context);
            toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
            refToast = new WeakReference<Toast>(toast);
            toast.setView(view);
            toast.show();
        } else
            showToastOnUiThread(context, null, view, duration, gravity);
         */
    }

    private static void showToastOnUiThread(final Context context, final CharSequence text, final View view, final int duration, final int gravity) {

        Handler handler = null;
        if (refHandler == null || (handler = refHandler.get()) == null)
            refHandler = new WeakReference<Handler>(handler = new Handler(
                    context.getMainLooper()));
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast toast = getToast();
                if (toast != null) toast.cancel();
                toast = null;
                if (view != null) {
                    toast = new Toast(context);
                    toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
                    toast.setView(view);
                    toast.show();
                } else if (text != null) {
                    toast = Toast.makeText(context, text, duration);
                    toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
                    toast.show();
                }
                if (toast != null)
                    refToast = new WeakReference<Toast>(toast);
            }
        });
    }
}
