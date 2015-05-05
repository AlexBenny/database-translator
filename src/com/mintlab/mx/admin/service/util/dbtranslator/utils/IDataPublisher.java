package com.mintlab.mx.admin.service.util.dbtranslator.utils;

import java.util.Map;

import java.io.*;

import java.sql.*;


public interface IDataPublisher {

	public File[] translate(String worksheet_class, Map<String,Connection> connectionMap, int appPk, String[] languages, String dbsResourcesFolder);
	public String version();
	
}
