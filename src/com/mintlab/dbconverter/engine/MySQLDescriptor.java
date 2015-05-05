package com.mintlab.dbconverter.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.mintlab.dbconverter.ColumnDescriptor;
import com.mintlab.dbconverter.Log;
import com.mintlab.dbconverter.PerformDBConversion;
import com.mintlab.dbconverter.TableDescriptor;


public class MySQLDescriptor extends DBDescriptor {

	private Connection connMySQLService = null;

	public MySQLDescriptor(String dbName, String host, int port, String user, String password)
		throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		super();
		this.name = dbName;
		this.dbType = DBDescriptor.MYSQL;
		connectMySQLService(host,port,user,password);
		try {
			PreparedStatement pstatMySQL = connMySQLService.prepareStatement(
			"SELECT TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_KEY, COLUMN_TYPE FROM COLUMNS WHERE TABLE_SCHEMA = ?");
			pstatMySQL.setString(1, dbName);
			ResultSet rs = pstatMySQL.executeQuery();
			//
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				if (this.get(tableName) == null) {
					this.add(new TableDescriptor(tableName));
				}
				ColumnDescriptor cd = new ColumnDescriptor(rs.getString("COLUMN_NAME"),rs.getString("COLUMN_TYPE"));
				cd.setColumnKey(rs.getString("COLUMN_KEY"));
				this.get(tableName).add(cd);
			}
		} catch (Exception e) {
			Log.error("Errore <loadMetadataSrcDB>: " + e.getMessage());
			e.printStackTrace();
		}
		//
		disconnectMySQLService();
		//
		defineTableRelations();
	}
	

	private void connectMySQLService(String host, int port, String user, String pass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		connMySQLService = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/information_schema?user="+user+"&password="+pass);
	}

	
	private void disconnectMySQLService() {
		try {
			connMySQLService.close();
		} catch (SQLException e) {
			Log.error("Error on disconnect MySQL: "+e);
		}
	}

}