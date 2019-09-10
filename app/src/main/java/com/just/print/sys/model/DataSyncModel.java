package com.just.print.sys.model;

import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.Printer;
import com.just.print.sys.server.DefaultService;

import java.util.List;

/**
 * 数据同步
 * Created by wangx on 2016/10/31.
 */
public class DataSyncModel extends AbsModel {

    private int maxVersion;
    private int maxPV;
    private List<Printer> printers;
    private int maxCV;
    private List<Category> categories;
    private int maxMarV;
    private List<Mark> marks;
    private int maxMV;
    private List<Menu> menus;

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public List<Printer> getPrinters() {
        return printers;
    }

    public void setPrinters(List<Printer> printers) {
        this.printers = printers;
    }

    public int getMaxPV() {
        return maxPV;
    }

    public void setMaxPV(int maxPV) {
        this.maxPV = maxPV;
    }

    public int getMaxCV() {
        return maxCV;
    }

    public void setMaxCV(int maxCV) {
        this.maxCV = maxCV;
    }

    public int getMaxMarV() {
        return maxMarV;
    }

    public void setMaxMarV(int maxMarV) {
        this.maxMarV = maxMarV;
    }

    public int getMaxMV() {
        return maxMV;
    }

    public void setMaxMV(int maxMV) {
        this.maxMV = maxMV;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }


    public String getService() {
        return DefaultService.class.getName();
    }


    @Override
    public int getInnerType() {
        return 2;
    }
}
