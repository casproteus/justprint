package com.just.print.sys.model;

import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;

import java.util.ArrayList;
import java.util.List;

public class SelectionDetail {

	private Menu dish;
	private int dishNum;
	private String pIP;
	private List<Mark> markList;//don't know why, but if I initialize it here, it will still be null when getting it....

	public List<Mark> getMarkList() {
		if(markList == null){
			markList = new ArrayList<Mark>();
		}
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
