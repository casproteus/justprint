package com.just.print_night.sys.server;

import com.just.print_night.sys.model.SelectionDetail;

import java.util.ArrayList;
import java.util.List;

public class CustomerSelection {

    private static CustomerSelection instance;
    private String tableName;
    private List<SelectionDetail> selectedDishes;
    private List<String> mkList;

    public static CustomerSelection getInstance() {
        if (instance == null) {
            instance = new CustomerSelection();
        }
        return instance;
    }

    private CustomerSelection() {
        tableName ="";
        selectedDishes = new ArrayList<SelectionDetail>();
    }

    public int addSelectedDish(SelectionDetail mn) {
        selectedDishes.add(mn);
        return 0;
    }

    public int deleteSelectedDish(SelectionDetail i) {
        selectedDishes.remove(i);
        return 0;
    }

    public int deleteSelectedDish(int i) {
        selectedDishes.remove(i);
        return 0;
    }

    public int clearMenu() {
        selectedDishes.clear();
        return 0;
    }

    public List<SelectionDetail> getSelectedDishes() {
        return selectedDishes;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
