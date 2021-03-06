package com.just.print.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.just.print.db.bean.SaleRecord;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table SALE_RECORD.
*/
public class SaleRecordDao extends AbstractDao<SaleRecord, Long> {

    public static final String TABLENAME = "SALE_RECORD";

    /**
     * Properties of entity SaleRecord.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Mname = new Property(1, String.class, "mname", false, "M_NAME");
        public final static Property Number = new Property(2, Double.class, "number", false, "NUMBER");
        public final static Property Price = new Property(3, Double.class, "price", false, "PRICE");
        public final static Property State = new Property(4, Integer.class, "state", false, "STATE");
        public final static Property Version = new Property(5, Long.class, "version", false, "VERSION");
    };

    private DaoSession daoSession;


    public SaleRecordDao(DaoConfig config) {
        super(config);
    }
    
    public SaleRecordDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'SALE_RECORD' (" + //
                "'_id' INTEGER PRIMARY KEY ASC UNIQUE ," + // 0: id
                "'M_NAME' TEXT," + // 1: mname
                "'NUMBER' REAL," + // 2: number
                "'PRICE' REAL," + // 3: price
                "'STATE' INTEGER," + // 4: state
                "'VERSION' INTEGER);"); // 5: version
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'SALE_RECORD'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, SaleRecord entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String mname = entity.getMname();
        if (mname != null) {
            stmt.bindString(2, mname);
        }
 
        Double number = entity.getNumber();
        if (number != null) {
            stmt.bindDouble(3, number);
        }
 
        Double price = entity.getPrice();
        if (price != null) {
            stmt.bindDouble(4, price);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(5, state);
        }
 
        Long version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(6, version);
        }
    }

    @Override
    protected void attachEntity(SaleRecord entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public SaleRecord readEntity(Cursor cursor, int offset) {
        SaleRecord entity = new SaleRecord( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // mname
            cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2), // number
            cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3), // price
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // state
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5) // version
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, SaleRecord entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMname(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setNumber(cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2));
        entity.setPrice(cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3));
        entity.setState(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setVersion(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(SaleRecord entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(SaleRecord entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
