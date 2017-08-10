package com.just.print.util;

import android.content.Context;
import android.os.Environment;

import com.just.print.app.AppData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DatabaseUtil {

    final private static String DATABASE_NAME_PREFFIX = "JustPrinter_"; //data/user/0/com.just.print/databases/
    final private static String DATABASE_NAME_SUFFIX = ".sqlite";
    //example WhatsApp :  /data/data/com.whatsapp/databases/msgstore.db

    /**
     * Call this method from any activity in your app (
     * for example ->    DatabaseUtil.copyDatabaseToExtStg(MainActivity.this);
     * this method will copy the database of your application onto SERVER
     */
    public static void copyDatabaseToExtStg(Context ctx) {
        //external storage file
//        File externalDirectory = new File(FOLDER_EXTERNAL_DIRECTORY);
//        if(!externalDirectory.exists())
//            externalDirectory.mkdirs();
//        File toFile = new File(externalDirectory, DATABASE_NAME);
        //internal storage file
        //https://developer.android.com/reference/android/content/Context.html#getDatabasePath(java.lang.String)
        File fromFile = ctx.getDatabasePath(DATABASE_NAME_PREFFIX + AppData.getShopName(ctx) + DATABASE_NAME_SUFFIX);
        //example WhatsApp :  /data/data/com.whatsapp/databases/msgstore.db
        if (fromFile.exists())
            //todo send file onto server.
            send(fromFile);
    }


    //______________________________________________________________________________________________ Utility function
    /**
     * @param file source location
     * copy file from 1 location to another
     */
    static void send(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            FileChannel src = is.getChannel();
//            FileOutputStream os = new FileOutputStream(toFile);
//            FileChannel dst = os.getChannel();
//            dst.transferFrom(src, 0, src.size());
//            src.close();	is.close();
//            dst.close();	os.close();
        } catch (Exception e) {
            //todo in case of exception
        }
    }
}
