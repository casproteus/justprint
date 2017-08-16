package com.just.print.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

import com.just.print.db.dao.PrinterDao;
import com.just.print.db.dao.CategoryDao;
import com.just.print.db.dao.MenuDao;
import com.just.print.db.dao.M2M_MenuPrintDao;
import com.just.print.db.dao.MarkDao;
import com.just.print.db.dao.LogDao;
import com.just.print.db.dao.DeviceDao;
import com.just.print.db.dao.SaleRecordDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * Master of DAO (schema version 1): knows all DAOs.
*/
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 1;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
        PrinterDao.createTable(db, ifNotExists);
        CategoryDao.createTable(db, ifNotExists);
        MenuDao.createTable(db, ifNotExists);
        M2M_MenuPrintDao.createTable(db, ifNotExists);
        MarkDao.createTable(db, ifNotExists);
        LogDao.createTable(db, ifNotExists);
        DeviceDao.createTable(db, ifNotExists);
        SaleRecordDao.createTable(db, ifNotExists);
    }
    
    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
        PrinterDao.dropTable(db, ifExists);
        CategoryDao.dropTable(db, ifExists);
        MenuDao.dropTable(db, ifExists);
        M2M_MenuPrintDao.dropTable(db, ifExists);
        MarkDao.dropTable(db, ifExists);
        LogDao.dropTable(db, ifExists);
        DeviceDao.dropTable(db, ifExists);
        SaleRecordDao.dropTable(db, ifExists);
    }
    
    public static abstract class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }
    
    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

    public DaoMaster(SQLiteDatabase db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(PrinterDao.class);
        registerDaoClass(CategoryDao.class);
        registerDaoClass(MenuDao.class);
        registerDaoClass(M2M_MenuPrintDao.class);
        registerDaoClass(MarkDao.class);
        registerDaoClass(LogDao.class);
        registerDaoClass(DeviceDao.class);
        registerDaoClass(SaleRecordDao.class);
    }
    
    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }
    
    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }
    
}
