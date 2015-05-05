package com.mintlab.mx.admin.service.util.dbtranslator.db.drivers;

import java.sql.*;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;
import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;


public class DBMySql extends DBManagerAbstract {
	private String dbName;
	private String host;
	private int port;
	private String user;
	private String pass;
	
	public DBMySql(String host, int port, String dbName, String user, String pass) {
		this.dbName = dbName;
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}
	
	public boolean close() {
		try {
			dbConnection.close();
			dbConnection = null;
			return true;
		} catch (SQLException e) {
			Log.error("Impossibile disconnettere MySQL: "+e.toString(),e);
			return false;
		}
	}

	public boolean open() {
		//DB Connection
		if (dbConnection != null) return true;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+dbName+"?user="+user+"&password="+pass);
			return true;
		}
		catch (Exception e) {
			Log.error("Impossibile connettere MySQL: "+e.toString(),e);
			return false;
		}
	}

}
