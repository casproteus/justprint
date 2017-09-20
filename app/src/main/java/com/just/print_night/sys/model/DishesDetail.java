package com.just.print_night.sys.model;

import com.just.print_night.db.bean.Menu;

public class DishesDetail {
    private Menu dish;
    private int dishNum;

    public Menu getDish() {
        return dish;
    }

    public void setDish(Menu dish) {
        this.dish = dish;
    }

    public int getDishNum() {
        return dishNum;
    }

    public void setDishNum(int dishNum) {
        this.dishNum = dishNum;
    }


}
