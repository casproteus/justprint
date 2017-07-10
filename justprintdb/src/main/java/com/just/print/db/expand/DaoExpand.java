package com.just.print.db.expand;

import android.database.Cursor;

import com.just.print.db.bean.Category;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.Printer;
import com.just.print.db.dao.MenuDao;
import com.just.print.db.dao.PrinterDao;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

/**
 * Created by wangx on 2016/11/2.
 */
public final class DaoExpand {
    /**
     * 根据类id查询menu
     *
     * @param menuDao
     * @param category
     * @return
     */
    static public List<Menu> queryMenuByCategory(MenuDao menuDao, Category category) {
        return addQueryBuilderNotDelete(menuDao.queryBuilder().where(MenuDao.Properties.Cid.eq(category.getId()))).list();
    }

    public static final <T> QueryBuilder<T> addQueryBuilderNotDelete(QueryBuilder<T> builder) {
        return builder.where(MenuDao.Properties.State.notEq(State.delete));
    }

    static public <T, K> List<T> queryNotDeleteAll(AbstractDao<T, K> dao) {
        return queryNotDeleteAllQuery(dao).list();
    }
    static public <T, K> QueryBuilder<T> queryNotDeleteAllQuery(AbstractDao<T, K> dao) {
        return dao.queryBuilder().where(MenuDao.Properties.State.notEq(State.delete));
    }

    static public List<Menu> queryMenuByCategory(Category category, MenuDao dao) {
        return dao.queryBuilder().where(MenuDao.Properties.Cid.eq(category.getId()), MenuDao.Properties.State.notEq(State.delete)).list();
    }

    static public <T, K> List<T> queryNotDeleteAll(AbstractDao<T, K> dao, String shop) {
        return dao.queryBuilder().where(MenuDao.Properties.State.notEq(State.delete)).list();
    }

    static public <T, K> List<T> queryDeleteAll(AbstractDao<T, K> dao) {
        return dao.queryBuilder().where(MenuDao.Properties.Version.eq(State.delete)).list();
    }

    /**
     * 模糊查询
     */
    static public List<Menu> queryFuzzyMenu(MenuDao menuDao, String ID) {
        return menuDao.queryBuilder().where(MenuDao.Properties.ID.like("%" + ID + "%")).list();
    }

    static public long queryMaxVersion(AbstractDao dao) {

        long result = -1;
        Cursor c = dao.getDatabase().rawQuery(String.format("select max(%s) from %s", "VERSION", dao.getTablename()), new String[]{});
        if (c.moveToFirst())
            result = c.getLong(0);
        c.close();
        return result;
    }

    /**
     * \
     * 获得主打印机 没有 则返回null
     */
    public static Printer getFirstPrint(PrinterDao printerDao) {
        List<Printer> list = printerDao.queryBuilder().where(PrinterDao.Properties.FirstPrint.eq(1)).limit(1).list();
        return list.size() > 0 ? list.get(0) : null;
    }

//    public static void updateAllPrintTo0(PrinterDao printerDao) {
//        List<Printer> list = printerDao.queryBuilder().where(PrinterDao.Properties.FirstPrint.eq(1)).list();
//        for (Printer p : list) {
//            p.setFirstPrint(0);
//            p.updateAndUpgrade();
//        }
//
//    }
}
