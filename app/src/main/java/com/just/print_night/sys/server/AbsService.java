package com.just.print_night.sys.server;

import com.just.print_night.net.UDPService;
import com.just.print_night.sys.model.AbsModel;

import java.net.InetSocketAddress;

/**
 * Created by wangx on 2016/10/31.
 */
abstract public class AbsService {

    abstract public boolean execute(AbsModel model, UDPService udpService, InetSocketAddress ch);

}
