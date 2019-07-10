package com.just.print_night.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.just.print_night.db.bean.Mark;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table MARK.
*/
public class MarkDao extends AbstractDao<Mark, String> {

    public static final String TABLENAME = "MARK";

    /**
     * Properties of entity Mark.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Name = new Property(0, String.class, "name", true, "NAME");
        public final static Property State = new Property(1, Integer.class, "state", false, "STATE");
        public final static Property Version = new Property(2, Long.class, "version", false, "VERSION");
    };

    private DaoSession daoSession;


    public MarkDao(DaoConfig config) {
        super(config);
    }
    
    public MarkDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'MARK' (" + //
                "'NAME' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: name
                "'STATE' INTEGER," + // 1: state
                "'VERSION' INTEGER);"); // 2: version
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'MARK'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Mark entity) {
        stmt.clearBindings();
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(1, name);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(2, state);
        }
 
        Long version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(3, version);
        }
    }

    @Override
    protected void attachEntity(Mark entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Mark readEntity(Cursor cursor, int offset) {
        Mark entity = new Mark( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // name
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // state
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2) // version
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Mark entity, int offset) {
        entity.setName(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setState(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setVersion(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(Mark entity, long rowId) {
        return entity.getName();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(Mark entity) {
        if(entity != null) {
            return entity.getName();
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