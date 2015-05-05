package com.mintlab.mx.admin.service.util.dbtranslator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import com.mintlab.mx.admin.service.util.dbtranslator.db.DBManagerAbstract;


public interface IWorksheet {
	
	public void init(Map<String,Connection> connectionMap, int appPk, String language, String appResourcesFolder);
	public ArrayList<DBManagerAbstract> getDbSources();
	public String treeMap();
	public DBManagerAbstract getDbDest(String langDbDestPath);

}
