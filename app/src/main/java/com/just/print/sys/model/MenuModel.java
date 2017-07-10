package com.just.print.sys.model;

import java.util.List;

/**
 * Created by qiqi on 2016/11/9.
 */

public class MenuModel {
    String  Name;
    int     Num;
    List<String> markList;
    public MenuModel() {
    }
/*
    public Menu(Long id) {
        this.id = id;
    }

    public Menu(String mname, Long id, Long pid, Long cid, String shopId, Integer state, Long version) {
        this.mname = mname;
        this.id = id;
        this.pid = pid;
        this.cid = cid;
        this.shopId = shopId;
        this.state = state;
        this.version = version;
    }
    */
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getNum() {
        return Num;
    }

    public void setNum(int num) {
        Num = num;
    }

    public List<String> getMarkList() {
        return markList;
    }

    public void setMarkList(List<String> markList) {
        this.markList = markList;
    }
}
