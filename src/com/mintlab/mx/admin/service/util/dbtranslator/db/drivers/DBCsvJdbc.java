package com.mintlab.mx.admin.service.util.dbtranslator.db.drivers;

import java.sql.*;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;
import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;


// http://csvjdbc.sourceforge.net/
public class DBCsvJdbc extends DBManagerAbstract {
	private String connectionString;
	
	public DBCsvJdbc(String connectionString) {
		this.connectionString = connectionString;
	}
	
	public boolean close() {
		try {
			dbConnection.close();
			dbConnection = null;
			return true;
		} catch (SQLException e) {
			Log.error("Impossibile disconnettere DB CSV-JDBC: "+e.toString(),e);
			return false;
		}
	}

	public boolean open() {
		//DB Connection
		if (dbConnection != null) return true;
		try {
			Class.forName("org.relique.jdbc.csv.CsvDriver").newInstance();
			dbConnection = DriverManager.getConnection(connectionString);
			return true;
		}
		catch (Exception e) {
			Log.error("Impossibile connettere DB CSV-JDBC: "+e.toString(),e);
			return false;
		}
	}

}
