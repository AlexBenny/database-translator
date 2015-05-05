package com.mintlab.mx.admin.service.util.dbtranslator;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

public class Data extends HashMap<String, Object> {

	public Data() {
		super();
	}
	
	public Data(HashMap<String,Object> data) {
		super(data);
	}

	public Data(ResultSet rs) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			int colNumber = rsmd.getColumnCount();
			for (int i=1; i<=colNumber; ++i){
				this.put(rsmd.getColumnName(i),rs.getObject(i));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public Timestamp getTimestamp(String columnName) { return (Timestamp)get(columnName); }
	public Integer   getInt(String columnName) 	 	 { return (Integer)get(columnName); }
	public Float	 getFloat(String columnName)     { return (Float)get(columnName); }
	public Long      getLong(String columnName)      { return (Long)get(columnName); }
	public Double    getDouble(String columnName)    { return (Double)get(columnName); }
	public Object    getObject(String columnName)    { return get(columnName); }
	public String    getString(String columnName)    {
		Object obj = get(columnName);
		return obj == null ? null : obj instanceof String ? (String)obj : obj.toString();
	}


}
