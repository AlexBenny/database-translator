package com.mintlab.mx.admin.service.util.dbtranslator;

import java.util.ArrayList;
import java.util.HashMap;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;

public class CustomSetup implements IActionSetup {

	protected ArrayList<Data> data;
	protected Data temp;
	
	public CustomSetup() {
		data = new ArrayList<Data>();
	}
	
	ArrayList<Data> getResult() {
		return data;
	}


	public void addRow() {
		temp = new Data();
		data.add(temp);
	}
	
	public void addRow(Data data) {
		temp = data;
		this.data.add(data);
	}
	
	public void setFieldValue(String columnName, Object value) {
		temp.put(columnName, value);
	}


	
}
