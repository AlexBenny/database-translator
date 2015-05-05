package com.mintlab.mx.admin.service.util.dbtranslator.db.drivers;

import java.sql.*;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;
import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;



public class DBSQLite extends DBManagerAbstract {

	private String pathDB;
	private String sqlDbStructure;

	public DBSQLite(String pathDB) {
		this.pathDB = pathDB;
	}
	
	public DBSQLite(String pathDB, String sqlDbStructure) {
		this.pathDB = pathDB;
		this.sqlDbStructure = sqlDbStructure;
	}

	public boolean close() {
		try {
			dbConnection.close();
			dbConnection = null;
			return true;
		} catch (SQLException e) {
			Log.error("Impossibile disconnettere SQLite: "+e.toString(),e);
			return false;
		}
	}

	public boolean open() {
		//DB Connection
		if (dbConnection != null) return true;
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
		} catch (Exception e) {
			Log.error("Error: "+e);
			return false;
		}
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:"+pathDB);
			if (sqlDbStructure != null && !sqlDbStructure.isEmpty()) {
				Statement cstat = dbConnection.createStatement();
				String[] sql = sqlDbStructure.split(";");
				for (int i=0; i<sql.length; i++) {
					int res = cstat.executeUpdate(sql[i]);					
				}
			}
		} catch (SQLException ex) {
			Log.error("Error on creation tables on SQLite: "+ex.getMessage(), ex);
			return false;
		}
		return true;
	}



}
