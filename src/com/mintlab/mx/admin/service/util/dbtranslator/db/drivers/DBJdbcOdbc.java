package com.mintlab.mx.admin.service.util.dbtranslator.db.drivers;

import java.sql.*;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;
import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;


public class DBJdbcOdbc extends DBManagerAbstract {
	private String connectionString;
	
	public DBJdbcOdbc(String connectionString) {
		this.connectionString = connectionString;
	}
	
	public boolean close() {
		try {
			dbConnection.close();
			dbConnection = null;
			return true;
		} catch (SQLException e) {
			Log.error("Impossibile disconnettere DB JDBC-ODBC: "+e.toString(),e);
			return false;
		}
	}

	public boolean open() {
		//DB Connection
		if (dbConnection != null) return true;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
			dbConnection = DriverManager.getConnection(connectionString);
			return true;
		}
		catch (Exception e) {
			Log.error("Impossibile connettere DB JDBC-ODBC: "+e.toString(),e);
			return false;
		}
	}

}
