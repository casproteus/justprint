package com.just.print.sys;

import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;
import com.just.print.sys.server.AbsService;
import com.just.print.sys.server.DefaultService;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangx on 2016/11/4.
 */
public class ServiceExecutor {


    static final Map<String, AbsService> serviceMap = new HashMap<>();

    static {
        serviceMap.put(DefaultService.class.getName(), new DefaultService());
    }

    static public void register(String serviceName, AbsService service) {
        serviceMap.put(serviceName, service);
    }

    static public void execute(AbsModel absModel, UDPService udpService, InetSocketAddress ch) {
        if (absModel != null) {
            AbsService service = serviceMap.get(absModel.getService());
            if (service != null) {
                service.execute(absModel, udpService, ch);
            }
        }

    }
}
