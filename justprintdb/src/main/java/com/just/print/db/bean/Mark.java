package com.just.print.db.bean;

import com.just.print.db.dao.DaoSession;

import de.greenrobot.dao.DaoException;

import com.just.print.db.dao.MarkDao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table MARK.
 */
public class Mark {

    private String name;
    private Integer state;
    private Long version;
    private int qt; //this property is transit not in db.
    /**
     * Used to resolve relations
     */
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    private transient MarkDao markDao;


    // KEEP FIELDS - put your custom fields here
    public boolean select;
    // KEEP FIELDS END

    public Mark() {
    }

    public Mark(String name) {
        this.name = name;
    }

    public Mark(String name, Integer state, Long version) {
        this.name = name;
        this.state = state;
        this.version = version;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        markDao = daoSession != null ? daoSession.getMarkDao() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getQt() { return qt; }

    public void setQt(int qt){ this.qt = qt; }

    /**
     * Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context.
     */
    public void delete() {
        if (markDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        markDao.delete(this);
    }

    /**
     * Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context.
     */
    public void update() {
        if (markDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        markDao.update(this);
    }

    /**
     * Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context.
     */
    public void refresh() {
        if (markDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        markDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here

    @Override
    public boolean equals(Object o) {

        return toString().equals(o.toString());
    }

    public void updateAndUpgrade() {
        //we use version as price so should not update version any more.
        //version = com.just.print.db.expand.DaoExpand.queryMaxVersion(markDao);
        update();
    }

    public void logicDelete() {
        state = com.just.print.db.expand.State.delete;
        updateAndUpgrade();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if(qt == 0) {
            return "";
        }else if(qt == 1) {
            return name;
        }else{
            return name + " x" + qt;
        }
    }

    public Mark clone(){
        Mark mark = new Mark();
        mark.setQt(getQt());
        mark.setName(getName());
        mark.setState(getState());
        mark.setVersion(getVersion());
        return mark;
    }

}
