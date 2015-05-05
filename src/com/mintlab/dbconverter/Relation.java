package com.mintlab.dbconverter;

public class Relation {

	public static final int RELATION_1N = -1;
	public static final int RELATION_N1 = +1;
	
	private TableDescriptor tableA, tableB;
	private String relationSQL;

	
	public Relation(TableDescriptor tdA, TableDescriptor tdB, String relationSQL) {
		this.tableA = tdA;
		this.tableB = tdB;
		this.relationSQL = relationSQL;
	}
	
	public TableDescriptor getTableSrcDescriptor() {
		return tableA;
	}

	public TableDescriptor getTableDestDescriptor() {
		return tableB;
	}

	public void setTable(TableDescriptor tdA, TableDescriptor tdB) {
		this.tableA = tdA;
		this.tableB = tdB;
	}

	public int getTypeRelation(TableDescriptor tableFocus) {
		return (tableFocus == tableA) ? RELATION_1N : RELATION_N1; 
	}

	public String getSQLRelation() {
		return relationSQL;
	}

	public void setSQLRelation(String relationSQL) {
		this.relationSQL = relationSQL;
	}
		
	public String toString() {
		return tableA.getName() + (tableB != null ? "-"+tableB.getName() : "");
	}

}