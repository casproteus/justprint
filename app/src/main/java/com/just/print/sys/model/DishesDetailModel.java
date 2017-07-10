package com.just.print.sys.model;

import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;

import java.util.List;

public class DishesDetailModel {
	private Menu dish;
	private int dishNum;



	private String pIP;
	List<Mark> markList;
	public DishesDetailModel() {
	}
	public List<Mark> getMarkList() {
		return markList;
	}

	public void setMarkList(List<Mark> markList) {
		this.markList = markList;
	}

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

	public String getpIP() {
		return pIP;
	}

	public void setpIP(String pIP) {
		this.pIP = pIP;
	}
}
