package com.mintlab.dbconverter.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.mintlab.dbconverter.ColumnDescriptor;
import com.mintlab.dbconverter.Log;
import com.mintlab.dbconverter.PerformDBConversion;
import com.mintlab.dbconverter.Relation;
import com.mintlab.dbconverter.TableDescriptor;


public abstract class DBDescriptor {
	
	public static final int MYSQL = 1;
	public static final int SQLITE = 2;


	protected String name;
	protected Connection connection;
	protected int dbType;
	protected HashMap<String, TableDescriptor> tables;

	
	public DBDescriptor() {
		tables = new HashMap<String, TableDescriptor>();
	}

	
	public TableDescriptor get(String tableName) {
		return tables.get(tableName);
	}

	public Collection<TableDescriptor> getTables() {
		return tables.values();
	}

	protected void add(TableDescriptor td) {
		tables.put(td.getName(),td);
	}

	protected TableDescriptor remove(String tableName) {
		return tables.remove(tableName);
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection conn) {
		this.connection = conn;
	}

	public int getDbType() {
		return dbType;
	}

	protected void setDbType(int dbType) {
		this.dbType = dbType;
	}

	public DBDescriptor clone() {
		DBDescriptor dbd = this.clone();
		dbd.setConnection(null);
		dbd.dbType = dbType;
		dbd.tables = new HashMap<String, TableDescriptor>();
		for (TableDescriptor td : tables.values()) {
			dbd.tables.put(td.getName(),td.clone());
		}
		dbd.defineTableRelations();
		return dbd;
	}

	public String toString() {
		String result = "";
		for (TableDescriptor td : tables.values()) {
			result += td.toString() + "\r\n";
		}
		return name + "\r\n" + result;
	}


	public void defineTableRelations() {
		try {
			// INTRODURRE QUI CLASSE DA CARICARE CON DEFINIZIONE RELAZIONI
			//		URL classUrl = classUrl = new URL("file:////Definitions.class");
			//		URL[] classUrls = { classUrl };
			//		URLClassLoader ucl = new URLClassLoader(classUrls);
			//		Class c = ucl.loadClass("Definitions");
			Class c = Class.forName("com.mintlab.dbconverter.engine.Definitions");
			LinkedList<Method> rulesTableRelation = new LinkedList<Method>();
			for (Method m : c.getDeclaredMethods()) {
				java.lang.annotation.Annotation[] annotations = m.getDeclaredAnnotations();
				for (Annotation annotation : annotations){
					if (annotation instanceof SpecificRelationAnnotation) {
						//adds a specific "relationship between tables" on list's head
						rulesTableRelation.addFirst(m);
						break;
					}
					if (annotation instanceof DefaultRelationAnnotation) {
						//adds a default "relationship between tables" on list's tail
						rulesTableRelation.addLast(m);
						break;
					}
				}
			}
			//assign relations
			for (TableDescriptor td : getTables()) {
				for (TableDescriptor td2 : getTables()) {
					if (td == td2) continue;
					Relation rel = null;
					Iterator<Method> itRules = rulesTableRelation.iterator();
					while (itRules.hasNext() && rel == null) {
						rel = (Relation)(itRules.next().invoke(c,td,td2));
					}
					td.addRelation(td2, rel);
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}


}