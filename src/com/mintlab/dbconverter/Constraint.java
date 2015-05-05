package com.mintlab.dbconverter;

public class Constraint {

	private String column;
	private ColumnDescriptor columnDescriptor;
	private Object value;
	
	
	public Constraint(String column, Object value) {
		this.column = column;
		this.value = value;
	}
	
	public Constraint(ColumnDescriptor column, Object value) {
		this.column = column.getName();
		this.columnDescriptor = column;
		this.value = value;
	}
	
	public String getColumnName() {
		return column;
	}
	
	public ColumnDescriptor getColumnDescriptor() {
		return columnDescriptor;
	}
	
	public Object getValue() {
		return value;
	}

	public Relation getRelationCnst(TableDescriptor td, int relation) {
		String sValue = (value instanceof String) ? "'"+value+"'" : value.toString();
		String resultSQL = td.getName()+"."+getColumnName()+" = "+sValue;
		return new Relation(td,null,resultSQL);
	}
	
}