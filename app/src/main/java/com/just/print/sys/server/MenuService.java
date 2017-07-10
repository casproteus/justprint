package com.just.print.sys.server;

import com.just.print.db.bean.Menu;
import com.just.print.sys.model.DishesDetailModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiqi on 2016/11/11.
 */

public class MenuService {
    private static MenuService instance;
    private String tableNum;
    private List<DishesDetailModel> ddList;
    private List<String> mkList;

    public static MenuService getInstance() {
        if (instance == null) {
            instance = new MenuService();
        }
        return instance;
    }

    public MenuService() {
        ddList = new ArrayList<DishesDetailModel>();
        this.tableNum = "";
    }

    public int addMenu(DishesDetailModel mn) {
        ddList.add(mn);
        return 0;
    }

    public int delMenu(DishesDetailModel i) {
        ddList.remove(i);
        return 0;
    }
    public int delMenu(int i) {
        ddList.remove(i);
        return 0;
    }
    public int clearMenu() {
        ddList.clear();
        return 0;
    }
    public List<DishesDetailModel> getMenu() {
        return ddList;
    }

    public String getTableNum() {
        return tableNum;
    }

    public void setTableNum(String tableNum) {
        this.tableNum = tableNum;
    }
}
