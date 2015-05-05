package com.mintlab.mx.admin.service.util.dbtranslator;

import java.util.ArrayList;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;

public class QuerySetup implements IActionSetup {

	protected String query;
	protected ArrayList<Integer> paramTypes;
	protected ArrayList params;
	protected DBManagerAbstract targetDB;
	

	public QuerySetup() {
		this("",null,new ArrayList<Integer>(),new ArrayList());
	}
	
	public QuerySetup(String query, DBManagerAbstract targetDB) {
		this(query,targetDB,new ArrayList<Integer>(),new ArrayList());
	}

	public QuerySetup(String query, DBManagerAbstract targetDB, ArrayList<Integer> paramTypes, ArrayList params) {
		this.query = query;
		this.targetDB = targetDB;
		this.paramTypes = paramTypes;
		this.params = params;
	}
	

	String getQuery() {
		return query;
	}


	ArrayList<Integer> getParamTypes() {
		return paramTypes;
	}


	ArrayList getParams() {
		return params;
	}


	DBManagerAbstract getTargetDB() {
		return targetDB;
	}

	
}
