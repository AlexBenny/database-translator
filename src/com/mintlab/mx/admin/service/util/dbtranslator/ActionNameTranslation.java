package com.mintlab.mx.admin.service.util.dbtranslator;

import java.sql.SQLException;
import java.util.HashMap;

import org.w3c.dom.Node;


public class ActionNameTranslation extends Action {

	protected String columnName;
	protected String lang;
	protected HashMap<String,String> dictionary = new HashMap<String,String>();

	
	@Override
	public final IActionSetup setupData() {
		 return new EmptySetup();
	}
	@Override
	public final boolean behaviour(Data data, Node clone) throws Exception {
		bindContent(columnName,dictionary.get(lang));
		return true;
	}
	
	
	public void setColumnName(String columnName, String lang) {
		this.columnName = columnName;
		this.lang = lang;
	}
	
	public void setNameTranslation(String lang, String nameTranslation) {
		dictionary.put(lang, nameTranslation);
	}

}