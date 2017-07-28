package com.just.print.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;

import com.alibaba.fastjson.JSONObject;
import com.just.print.util.L;

@SuppressLint("SimpleDateFormat")
public class XCrashHandler implements UncaughtExceptionHandler {
	private Context mContext;

	private UncaughtExceptionHandler mDefaultHandler;

	private JSONObject devInfo() {
		JSONObject json = new JSONObject();
		json.putAll(getSystemInfo());
		json.putAll(getApplicationInfo());
		return json;
	}

	public JSONObject getApplicationInfo() {
		PackageManager pm = null;
		ApplicationInfo appinfo = null;
		PackageInfo pi;
		JSONObject json = new JSONObject();
		try {
			pm = mContext.getPackageManager();
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			appinfo = pm.getApplicationInfo(mContext.getPackageName(), 0);
			json.put("APP_Name", pm.getApplicationLabel(appinfo));
			json.put("APP_Version", pi.versionCode);
			json.put("APP_VersionName", pi.versionName);
			json.put("APP_PackageName", pi.packageName);
			json.put("APP_LastUpdateTime", pi.lastUpdateTime);
			json.put("APP_FirstInstallTime", pi.firstInstallTime);
		} catch (Exception e) {
		}
		return json;
	}

	public JSONObject getSystemInfo() {
		JSONObject json = new JSONObject();
		json.put("SYS_SDK_INT", android.os.Build.VERSION.SDK_INT);
		json.put("SYS_SDK", android.os.Build.VERSION.CODENAME);
		json.put("SYS_PRODUCT", android.os.Build.PRODUCT);
		json.put("SYS_BRAND", android.os.Build.BRAND);// 品牌
		json.put("SYS_CPU_ABI", android.os.Build.CPU_ABI);// cpu 平台
		json.put("SYS_CPU_ABI2", android.os.Build.CPU_ABI2);// cpu
		json.put("SYS_DEVICE", android.os.Build.DEVICE);
		json.put("SYS_DISPLAY", android.os.Build.DISPLAY);
		json.put("SYS_HARDWARE", android.os.Build.HARDWARE);
		json.put("SYS_MANUFACTURER", android.os.Build.MANUFACTURER);
		json.put("SYS_MODEL", android.os.Build.MODEL);
		json.put("SYS_SERIAL", android.os.Build.SERIAL);
		return json;
	}

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = getDefaultHandler(Thread
				.getDefaultUncaughtExceptionHandler());
		Thread.setDefaultUncaughtExceptionHandler(this);

	}

	private static UncaughtExceptionHandler getDefaultHandler(
			UncaughtExceptionHandler ex) {
		if (ex instanceof XCrashHandler)
			return getDefaultHandler(((XCrashHandler) ex).getDefaultHandler());
		else
			return ex;
	}

	private UncaughtExceptionHandler getDefaultHandler() {
		return mDefaultHandler;
	}

	private void save2File(String msg) throws IOException {
		String log = Environment.getExternalStorageDirectory()
				+ "/PDMOutput/log/";
		File file = new File(log,
				new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream os = new FileOutputStream(file, true);
		os.write(msg.getBytes(Charset.forName("UTF-8")));
		os.write("\r\n".getBytes());
		os.flush();
		os.close();
	}

	/**
	 * 保存到服务器
	 * 
	 * @param ex
	 * @param msg
	 */
	private void saveLog(final Thread thread, final Throwable ex, String msg) {
		L.e("xLog", msg, ex);
		JSONObject json = devInfo();
		json.put("throw_class", ex.getClass());
		json.put("throw_msg", msg);
		json.put("tag", "crash");
		json.put("localtime", System.currentTimeMillis());
		try {
			save2File(json.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void showMessage() {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				//ToastUtil.showToast(mContext, "程序崩溃了");
				Looper.loop();
			}
		}.start();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// showMessage();
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			printWriter.print("\r\n");
			cause = cause.getCause();
		}
		printWriter.println();
		printWriter.close();
		saveLog(thread, ex, writer.toString());
		mDefaultHandler.uncaughtException(thread, ex);
	}
}
