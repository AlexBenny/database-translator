package com.mintlab.mx.admin.service.util.dbtranslator.db.drivers;

import java.sql.*;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;
import com.mintlab.mx.admin.service.util.dbtranslator.util.Log;


public class DBExtConn extends DBManagerAbstract {
	
	public DBExtConn(Connection connection) {
		this.dbConnection = connection;
	}
	
	public boolean close() {
		return true;
	}

	public boolean open() {
		return true;
	}

}
