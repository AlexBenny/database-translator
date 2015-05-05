package com.mintlab.mx.admin.service.util.dbtranslator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;



public abstract class DBManagerAbstract {

	protected Connection dbConnection;
	protected int generatedKey;

	public DBManagerAbstract() {
		super();
	}
	
	public abstract boolean open();
	public abstract boolean close();


	public final ResultSet doQuery(String sql) {
		ArrayList<Integer> aTypes = new ArrayList<Integer>();
		ArrayList aObjs = new ArrayList();
		return doQuery(sql,aTypes,aObjs);
	}

	public final ResultSet doQuery(String sql, ArrayList<Integer> const__java_sql_Types, ArrayList params) {
		PreparedStatement pstat;
		try {
			pstat = dbConnection.prepareStatement(sql);
			for (int i=0; i<params.size(); i++) {
				Object p = params.get(i);
				if (p == null) {
					pstat.setNull(i+1, const__java_sql_Types.get(i).intValue()); 
				} else {
					pstat.setObject(i+1, p, const__java_sql_Types.get(i).intValue());
				}
			}
			return pstat.executeQuery();
		} catch (SQLException e) {
			Log.error("Impossibile eseguire la query: "+sql, e);
			return null;
		}
	}

	public final boolean doInsert(String sql) {
		ArrayList<Integer> aTypes = new ArrayList<Integer>();
		ArrayList aObjs = new ArrayList();
		return doInsert(sql,aTypes,aObjs);
	}	

	public final boolean doInsert(String sql, ArrayList<Integer> const__java_sql_Types, ArrayList params) {
		PreparedStatement pstat;
		try {
			dbConnection.setAutoCommit(false); //Start Transaction
			pstat = dbConnection.prepareStatement(sql);
			for (int i=0; i<params.size(); i++) {
				Object p = params.get(i);
				if (p == null) {
					pstat.setNull(i+1, const__java_sql_Types.get(i).intValue()); 
				} else {
					pstat.setObject(i+1, p, const__java_sql_Types.get(i).intValue());
				}
			}
			int result = pstat.executeUpdate();
			if (result > 0) {
				Statement stat = dbConnection.createStatement();
				ResultSet rsKeys = stat.executeQuery("SELECT last_insert_rowid()");
				if (rsKeys.next()) {
				    generatedKey = rsKeys.getInt(1);
				}
				rsKeys.close();
				stat.close();
				dbConnection.commit();
			}
			dbConnection.setAutoCommit(true);
			return (result > 0);
		} catch (SQLException e) {
			Log.error("Impossibile eseguire l'inserimento: "+sql+"\r\n"+e.getMessage(), e);
			return false;
		}		
	}
	
	public final int getGeneratedKey() {
		return generatedKey;
	}

}