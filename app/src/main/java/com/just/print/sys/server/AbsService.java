package com.just.print.sys.server;

import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;

import java.net.InetSocketAddress;

/**
 * Created by wangx on 2016/10/31.
 */
abstract public class AbsService {

    abstract public boolean execute(AbsModel model, UDPService udpService, InetSocketAddress ch);

}
