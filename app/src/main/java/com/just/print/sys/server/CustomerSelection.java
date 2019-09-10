package com.just.print.sys.server;

import com.just.print.sys.model.SelectionDetail;

import java.util.ArrayList;
import java.util.List;

public class CustomerSelection {

    private static CustomerSelection instance;
    private String tableNumber;
    private List<SelectionDetail> selectedDishes;
    private List<String> mkList;

    public static CustomerSelection getInstance() {
        if (instance == null) {
            instance = new CustomerSelection();
        }
        return instance;
    }

    private CustomerSelection() {
        tableNumber ="";
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

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }
}
