package com.just.print.db.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.just.print.db.bean.Printer;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Log;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.bean.Device;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig printerDaoConfig;
    private final DaoConfig categoryDaoConfig;
    private final DaoConfig menuDaoConfig;
    private final DaoConfig m2M_MenuPrintDaoConfig;
    private final DaoConfig markDaoConfig;
    private final DaoConfig logDaoConfig;
    private final DaoConfig saleRecordDaoConfig;
    private final DaoConfig deviceDaoConfig;

    private final PrinterDao printerDao;
    private final CategoryDao categoryDao;
    private final MenuDao menuDao;
    private final M2M_MenuPrintDao m2M_MenuPrintDao;
    private final MarkDao markDao;
    private final LogDao logDao;
    private final SaleRecordDao saleRecordDao;
    private final DeviceDao deviceDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        printerDaoConfig = daoConfigMap.get(PrinterDao.class).clone();
        printerDaoConfig.initIdentityScope(type);

        categoryDaoConfig = daoConfigMap.get(CategoryDao.class).clone();
        categoryDaoConfig.initIdentityScope(type);

        menuDaoConfig = daoConfigMap.get(MenuDao.class).clone();
        menuDaoConfig.initIdentityScope(type);

        m2M_MenuPrintDaoConfig = daoConfigMap.get(M2M_MenuPrintDao.class).clone();
        m2M_MenuPrintDaoConfig.initIdentityScope(type);

        markDaoConfig = daoConfigMap.get(MarkDao.class).clone();
        markDaoConfig.initIdentityScope(type);

        logDaoConfig = daoConfigMap.get(LogDao.class).clone();
        logDaoConfig.initIdentityScope(type);

        saleRecordDaoConfig = daoConfigMap.get(SaleRecordDao.class).clone();
        saleRecordDaoConfig.initIdentityScope(type);

        deviceDaoConfig = daoConfigMap.get(DeviceDao.class).clone();
        deviceDaoConfig.initIdentityScope(type);

        printerDao = new PrinterDao(printerDaoConfig, this);
        categoryDao = new CategoryDao(categoryDaoConfig, this);
        menuDao = new MenuDao(menuDaoConfig, this);
        m2M_MenuPrintDao = new M2M_MenuPrintDao(m2M_MenuPrintDaoConfig, this);
        markDao = new MarkDao(markDaoConfig, this);
        logDao = new LogDao(logDaoConfig, this);
        saleRecordDao = new SaleRecordDao(saleRecordDaoConfig, this);
        deviceDao = new DeviceDao(deviceDaoConfig, this);

        registerDao(Printer.class, printerDao);
        registerDao(Category.class, categoryDao);
        registerDao(Menu.class, menuDao);
        registerDao(M2M_MenuPrint.class, m2M_MenuPrintDao);
        registerDao(Mark.class, markDao);
        registerDao(Log.class, logDao);
        registerDao(SaleRecord.class, saleRecordDao);
        registerDao(Device.class, deviceDao);
    }
    
    public void clear() {
        printerDaoConfig.getIdentityScope().clear();
        categoryDaoConfig.getIdentityScope().clear();
        menuDaoConfig.getIdentityScope().clear();
        m2M_MenuPrintDaoConfig.getIdentityScope().clear();
        markDaoConfig.getIdentityScope().clear();
        logDaoConfig.getIdentityScope().clear();
        saleRecordDaoConfig.getIdentityScope().clear();
        deviceDaoConfig.getIdentityScope().clear();
    }

    public PrinterDao getPrinterDao() {
        return printerDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public MenuDao getMenuDao() {
        return menuDao;
    }

    public M2M_MenuPrintDao getM2M_MenuPrintDao() {
        return m2M_MenuPrintDao;
    }

    public MarkDao getMarkDao() {
        return markDao;
    }

    public LogDao getLogDao() {
        return logDao;
    }

    public SaleRecordDao getSaleRecordDao() {
        return saleRecordDao;
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

}
