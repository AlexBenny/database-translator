package com.mintlab.dbconverter;

import java.util.*;



public class TableDescriptor {
	
	private String name;
	private HashMap<String,ColumnDescriptor> columns;
	private HashMap<String,Relation> relations1N;
	private HashMap<String,Relation> relationsN1;
	private boolean composed;
	
	
	public TableDescriptor(String name) {
		this.name = name;
		composed = false;
		columns = new HashMap<String,ColumnDescriptor>();
		relations1N = new HashMap<String,Relation>();
		relationsN1 = new HashMap<String,Relation>();
	}
	
	public void setName(String name) { this.name = name; }
	public String getName() { return name; }
	
	public void add(ColumnDescriptor cd) {
		String colName = cd.getName();
		columns.put(colName, cd);
	}

	public ColumnDescriptor get(String name) {
		return columns.get(name);
	}
	
	public Collection<ColumnDescriptor> getColumns() {
		return columns.values();
	}

	public void addRelation(TableDescriptor tableDescriptor, Relation relation) {
		relations1N.put(tableDescriptor.name, relation);
		tableDescriptor.relationsN1.put(name, relation);
	}
	
	public Collection<Relation> getRelations1N() {
		return relations1N.values();
	}
	
	public Relation getRelation1N(String name) {
		return relations1N.get(name);
	}
	
	public Collection<Relation> getRelationsN1() {
		return relationsN1.values();
	}
	
	public Relation getRelationN1(String name) {
		return relationsN1.get(name);
	}
	
	public TableDescriptor clone() {
		TableDescriptor td = new TableDescriptor(name);
		td.columns = new HashMap<String, ColumnDescriptor>();
		for (ColumnDescriptor cd : columns.values()) {
			td.columns.put(cd.getName(),cd.clone());
		}
		return td;
	}

	public void setComposed() {
		composed = true;
	}

	public boolean isComposed() {
		return composed;
	}

	public boolean has(ColumnDescriptor cd) {
		return columns.containsValue(cd);
	}

	public boolean has(String columnName) {
		return columns.containsKey(columnName);
	}
	
	public String toString() {
		return name + " col: " + columns.values() +
			"\r\n 1N: " + relations1N.values() +
			"\r\n N1: " + relationsN1.values() + "\r\n";
	}
	
}