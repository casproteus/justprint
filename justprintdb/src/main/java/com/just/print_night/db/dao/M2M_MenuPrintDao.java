package com.just.print_night.db.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.just.print_night.db.bean.Menu;
import com.just.print_night.db.bean.Printer;

import com.just.print_night.db.bean.M2M_MenuPrint;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table M2_M__MENU_PRINT.
*/
public class M2M_MenuPrintDao extends AbstractDao<M2M_MenuPrint, Long> {

    public static final String TABLENAME = "M2_M__MENU_PRINT";

    /**
     * Properties of entity M2M_MenuPrint.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Pid = new Property(0, Long.class, "pid", false, "PID");
        public final static Property Mid = new Property(1, String.class, "mid", false, "MID");
        public final static Property Id = new Property(2, Long.class, "id", true, "_id");
        public final static Property State = new Property(3, Integer.class, "state", false, "STATE");
        public final static Property Version = new Property(4, Long.class, "version", false, "VERSION");
    };

    private DaoSession daoSession;

    private Query<M2M_MenuPrint> menu_M2M_MenuPrintListQuery;

    public M2M_MenuPrintDao(DaoConfig config) {
        super(config);
    }
    
    public M2M_MenuPrintDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'M2_M__MENU_PRINT' (" + //
                "'PID' INTEGER," + // 0: pid
                "'MID' TEXT," + // 1: mid
                "'_id' INTEGER PRIMARY KEY ASC UNIQUE ," + // 2: id
                "'STATE' INTEGER," + // 3: state
                "'VERSION' INTEGER);"); // 4: version
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'M2_M__MENU_PRINT'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, M2M_MenuPrint entity) {
        stmt.clearBindings();
 
        Long pid = entity.getPid();
        if (pid != null) {
            stmt.bindLong(1, pid);
        }
 
        String mid = entity.getMid();
        if (mid != null) {
            stmt.bindString(2, mid);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(3, id);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(4, state);
        }
 
        Long version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(5, version);
        }
    }

    @Override
    protected void attachEntity(M2M_MenuPrint entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2);
    }    

    /** @inheritdoc */
    @Override
    public M2M_MenuPrint readEntity(Cursor cursor, int offset) {
        M2M_MenuPrint entity = new M2M_MenuPrint( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // pid
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // mid
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // id
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // state
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4) // version
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, M2M_MenuPrint entity, int offset) {
        entity.setPid(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setState(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setVersion(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(M2M_MenuPrint entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(M2M_MenuPrint entity) {
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
    
    /** Internal query to resolve the "m2M_MenuPrintList" to-many relationship of Menu. */
    public List<M2M_MenuPrint> _queryMenu_M2M_MenuPrintList(String mid) {
        synchronized (this) {
            if (menu_M2M_MenuPrintListQuery == null) {
                QueryBuilder<M2M_MenuPrint> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.Mid.eq(null));
                menu_M2M_MenuPrintListQuery = queryBuilder.build();
            }
        }
        Query<M2M_MenuPrint> query = menu_M2M_MenuPrintListQuery.forCurrentThread();
        query.setParameter(0, mid);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getPrinterDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getMenuDao().getAllColumns());
            builder.append(" FROM M2_M__MENU_PRINT T");
            builder.append(" LEFT JOIN PRINTER T0 ON T.'PID'=T0.'_id'");
            builder.append(" LEFT JOIN MENU T1 ON T.'MID'=T1.'ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected M2M_MenuPrint loadCurrentDeep(Cursor cursor, boolean lock) {
        M2M_MenuPrint entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Printer print = loadCurrentOther(daoSession.getPrinterDao(), cursor, offset);
        entity.setPrint(print);
        offset += daoSession.getPrinterDao().getAllColumns().length;

        Menu menu = loadCurrentOther(daoSession.getMenuDao(), cursor, offset);
        entity.setMenu(menu);

        return entity;    
    }

    public M2M_MenuPrint loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<M2M_MenuPrint> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<M2M_MenuPrint> list = new ArrayList<M2M_MenuPrint>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<M2M_MenuPrint> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<M2M_MenuPrint> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
