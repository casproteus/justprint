package com.just.print.Thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by qiqi on 2016/11/3.
 */

public class PrintThreadPool {
    private static PrintThreadPool instance = null;
    private ExecutorService pool;
    public static PrintThreadPool getInstance(){
        if(instance == null){
            instance = new PrintThreadPool();
        }
        return instance;
    }

    public PrintThreadPool(){
        pool = Executors.newSingleThreadExecutor();

    }
    public ExecutorService getPrintThreadPool(){
        return pool;
    }

}
