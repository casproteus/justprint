package com.just.print.sys.server;

import com.just.print.app.Applic;
import com.just.print.net.UDPService;
import com.just.print.sys.model.AbsModel;
import com.just.print.sys.model.QueryShopRequest;
import com.just.print.sys.model.QueryShopResult;
import com.just.print.util.Base64Util;
import com.just.print.util.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * Created by wangx on 2016/10/31.
 */
public class DefaultService extends AbsService {
    static final String tag = "DefaultService";

    @Override
    public boolean execute(AbsModel model, UDPService udpService, InetSocketAddress ch) {
        switch (model.getInnerType()) {
            case 1:
                QueryShopRequest queryShop = model.typeCoercion();
                if (queryShop != null)
                    queryShop(queryShop, udpService, ch);
                break;
        }
        return false;
    }

    private void queryShop(QueryShopRequest queryShop, UDPService udpService, InetSocketAddress ch) {
        L.d(tag, "queryShop");
        QueryShopResult result = new QueryShopResult();

        File dbfile = Applic.getApp().getDBPath(queryShop.getShopName());
        if (dbfile.exists()) {
            result.setExists(true);
            result.setFileSize(dbfile.length());
            result.setShopName(queryShop.getShopName());
            try {
                InputStream is = new FileInputStream(dbfile);
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                result.setFileBase64(Base64Util.encodeToString(bytes));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            result.setExists(false);
        }
        udpService.sendResult(queryShop, result, ch);
    }
}
