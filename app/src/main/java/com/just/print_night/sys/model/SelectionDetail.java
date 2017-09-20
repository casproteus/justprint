package com.just.print_night.sys.model;

import com.just.print_night.db.bean.Mark;
import com.just.print_night.db.bean.Menu;

import java.util.List;

public class SelectionDetail {

	private Menu dish;
	private int dishNum;
	private String pIP;
	private List<Mark> markList;

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
