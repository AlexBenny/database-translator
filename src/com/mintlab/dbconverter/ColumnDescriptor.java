package com.mintlab.dbconverter;

public class ColumnDescriptor {
	
	private String name;
	private String dataType;
	private boolean primary = false;
	private boolean notnull = false;
	private boolean unique = false;
	private boolean imported;
	


	public ColumnDescriptor(String name, String dataType) {
		this.name = name;
		this.dataType = dataType;
	}

	public void setName(String columnName) { name = columnName; }
	public String getName() { return name; }
	
	public void setDataType(String dataType) { this.dataType = dataType; }	
	public String getDataType() { return dataType; }

	public String getSQLiteDataType() {
		if (dataType.startsWith("varchar")) return "TEXT";
		if (dataType.startsWith("int")) return "INTEGER";
		if (dataType.startsWith("double")) return "REAL";
		if (dataType.startsWith("datetime")) return "TEXT";
		return null;
	}
	
	
	public void setColumnKey(String key) {
		if (key.equals("PRI")) {
			primary = true;
			notnull = true;
			unique = true;
		}
		if (key.equals("MUL")) {
			notnull = true;
		}
		if (key.equals("UNI")) {
			unique = true;
		}
	}

	public boolean isUnique() { return unique; }
	public boolean isPrimary() { return primary; }
	public boolean isNotNull() { return notnull; }
	
	public ColumnDescriptor clone() {
		ColumnDescriptor cd = new ColumnDescriptor(name,dataType);
		cd.primary = primary;
		cd.notnull = notnull;
		cd.unique = unique;
		return cd;
	}

	public void setImported() {
		imported = true;
	}
	
	public boolean isImported() {
		return imported;
	}
	
	public String toString() {
		return name;
	}
	
}
