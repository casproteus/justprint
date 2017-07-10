package com.just.print.sys.server;

import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;

import java.net.InetSocketAddress;

/**
 * 数据同步服务
 * Created by wangx on 2016/10/31.
 */
public class DataSyncService extends AbsService {


    @Override
    public boolean execute(AbsModel model, UDPService udpService, InetSocketAddress address) {
        return false;
    }
}
