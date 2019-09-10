package com.just.print.sys.model;

import java.util.List;

public class PrintFormat {
    private String tableNumber;
    private List<DishesDetail> distesList;
    private String remark1;
    private String remark2;
    private String remark3;

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public List<DishesDetail> getDistesList() {
        return distesList;
    }

    public void setDistesList(List<DishesDetail> distesList) {
        this.distesList = distesList;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getRemark3() {
        return remark3;
    }

    public void setRemark3(String remark3) {
        this.remark3 = remark3;
    }

}
