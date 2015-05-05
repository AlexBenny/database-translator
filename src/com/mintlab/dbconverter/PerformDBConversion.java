package com.mintlab.dbconverter;


import com.mintlab.dbconverter.engine.*;

import java.io.*;
import java.sql.*;
import java.util.*;



/**
 * Handles requests for the application welcome page.
 */
public class PerformDBConversion {

	public static String HOST = "62.123.225.113";  //fake host
	public static int PORT = 3306;
	public static String DBNAME = "devmint";
	public static String USERNAME = "username";
	public static String PASSWORD = "password";

	private static String[] languages = new String[]{"it","en"};
	private static String[] tablesNotToExport = new String[]{"link","image","customer","iphone_app","iphone_device","device"};
	private static String path = "C:\\Documents and Settings\\Administrator\\Documenti\\Progetti\\SpringSourceWks\\DBConversion";
	private static String nameSQLiteDB = "mevents";  //nomefile database
	private static String tempSQLiteDB = "temp";  //directory temporanea
	
	private Connection connMySQL = null;
	public DBDescriptor dbDesc;
	private DBDescriptor dbDest;


	
	public static void main(String[] args) {
		new PerformDBConversion().performConversionMEvents("FakeMEventsDatabaseConverter");
//		new PerformDBConversion().performConversionMRestaurant("ristopizza");
	}
	

	/**
	 * mRestaurant
	 */ /*
	public void performConversionMRestaurant(String organization) {
		HOST = "127.0.0.1";
		DBNAME = "mrestaurant";
		USERNAME = "root";
		PASSWORD = "root";
		languages = new String[]{"it","en"};
		tablesNotToExport = new String[]{"link","image","customer","iphone_app","iphone_device","device"};
		path = "C:\\Documents and Settings\\Administrator\\Documenti\\Progetti\\Eclipse\\DBConverter";
		nameSQLiteDB = "mrestaurant";  //nomefile database
		tempSQLiteDB = "temp";  //directory temporanea
		//
		backup();
		dbDest.loadMetadataSrcDB(this, DBNAME);
		makeMetadataDestDB();
		adaptMetadataDestDBWithoutTranslations();
		makeDestDB();
		//
		LinkedList<TableDescriptor> tablesToExport = new LinkedList<TableDescriptor>();
		for (TableDescriptor td : dbDest.getTables()) {
			boolean present = false;
			for (String s : tablesNotToExport) {
				if (td.getName().equals(s)) {
					present = true;
					break;
				}
			}
			if (!present) tablesToExport.add(td);
		}
		//
		for (String lang : languages) {
			LinkedList<Constraint> constraints = new LinkedList<Constraint>();
			constraints.add(new Constraint("lang",lang));
			constraints.add(new Constraint(dbDest.get("organization").get("name"),organization));
			//
			Connection connSQLite = connectSQLite(getPathDBByLang(lang));
 			if (connSQLite == null) {
				Log.error("Impossibile connettersi a SQLite on lang: "+lang);
				return;
			}
 			if (!connectMySQL()) {
 				Log.error("Impossibile connettersi a MySQL");
 				return;
 			}
			Engine.populateDB(connMySQL, connSQLite, tablesToExport, constraints);
			disconnectMySQL();
			try { connSQLite.close(); } catch (SQLException e) {}
			//
		}
		System.out.println("finished");
	}
*/

	
	/**
	 * mEvents
	 */
	public void performConversionMEvents(String organization) {
		DBNAME = "devmint";
		HOST = "127.0.0.1";
		PORT = 3306;
		USERNAME = "root";
		PASSWORD = "root";
		languages = new String[]{"it","en"};
		tablesNotToExport = new String[]{"link","image","customer","iphone_app","iphone_device","device"};
		path = "C:\\Documents and Settings\\Administrator\\Documenti\\Progetti\\Eclipse\\DBConverter";
		nameSQLiteDB = "mevents";  //nomefile database
		tempSQLiteDB = "temp";  //directory temporanea
		//
		backup();
		try {
			dbDesc = new MySQLDescriptor(DBNAME, HOST, PORT, USERNAME, PASSWORD);
		} catch (Exception e) {
			Log.error("Error on DB connection: "+e.toString());
			//System.out.println(e);
			return;
		}
		dbDest = dbDesc.clone();
		Log.error("valuated DB:\r\n\r\n"+dbDesc);
		adaptMetadataDestDBWithoutTranslations();
		makeDestDB();
		//
		LinkedList<TableDescriptor> tablesToExport = new LinkedList<TableDescriptor>();
		for (TableDescriptor td : dbDest.getTables()) {
			boolean present = false;
			for (String s : tablesNotToExport) {
				if (td.getName().equals(s)) {
					present = true;
					break;
				}
			}
			if (!present) tablesToExport.add(td);
		}
		//
		for (String lang : languages) {
			//set constraints
			LinkedList<Constraint> constraints = new LinkedList<Constraint>();
			constraints.add(new Constraint("lang",lang));
			constraints.add(new Constraint(dbDest.get("organization").get("name"),organization));
			//
			Connection connSQLite = connectSQLite(getPathDBByLang(lang));
 			if (connSQLite == null) {
				Log.error("Impossibile connettersi a SQLite on lang: "+lang);
				return;
			}
 			if (!connectMySQL()) {
 				Log.error("Impossibile connettersi a MySQL");
 				return;
 			}
			Engine.populateDB(connMySQL, connSQLite, tablesToExport, constraints);
			disconnectMySQL();
			try { connSQLite.close(); } catch (SQLException e) {}
		}
		System.out.println("finished");
	}


	
	
	/*
	 * 
	 * COSTRUZIONE DB
	 * 
	 */

	private void makeMetadataDestDB() {
	}	


	/*
	 * 
	 * OPERAZIONI FILE
	 * 
	 */
	
	private void backup() {
		boolean result = false;
		copyfile(getPathDBByLang(tempSQLiteDB),getPathDBByLang(tempSQLiteDB)+"_bck");
		File f = new File(getPathDBByLang(tempSQLiteDB));
		result = f.delete();
		for (String lang : languages) {
			result = false;
			copyfile(getPathDBByLang(lang),getPathDBByLang(lang)+"_bck");
			f = new File(getPathDBByLang(lang));
			result = f.delete();
		}
	}


	private void adaptMetadataDestDBWithoutTranslations() {
		LinkedList<String> deletedTables = new LinkedList<String>();
		for (TableDescriptor td : dbDest.getTables()) {
			if (td.getName().endsWith("_translation")) {
				String tableName = td.getName();
				tableName = tableName.substring(0, tableName.length()-12);
				Relation r = td.getRelation1N(tableName);
				TableDescriptor tabOrig = r.getTableDestDescriptor();
				for (ColumnDescriptor cd : td.getColumns()) {
					String colName = cd.getName();
					if (cd.isPrimary() || colName.equals("lang") || colName.endsWith("_pk")) continue;
					cd.setImported();
					tabOrig.add(cd);
					tabOrig.setComposed();
				}
				deletedTables.add(td.getName());
			}
		}
		for (String deletedTable : deletedTables) {
			dbDest.remove(deletedTable);
		}
	}

	
	private void makeDestDB() {
		Connection connSQLite = connectSQLite(getPathDBByLang(tempSQLiteDB));
		if (connSQLite == null) {
			Log.error("Impossibile connettersi a SQLite");
			return;
		}
		//
		dbDest.setConnection(connSQLite);
		for (TableDescriptor td : dbDest.getTables()) {
			String query = "CREATE TABLE "+td.getName()+" (";
			for (ColumnDescriptor cd : td.getColumns()) {
				query += cd.getName() + " " + cd.getSQLiteDataType() + (cd.isPrimary()?" PRIMARY KEY ":"") + ", ";
			}
			query = query.substring(0, query.length()-2)+")";
			try {
				PreparedStatement pstatSQLite = connSQLite.prepareStatement(query);
				pstatSQLite.execute();
			} catch(SQLException e) {
				Log.error("Error on creation tables on SQLite: "+e);
			}
		}
		//
		for (String lang : languages) {
			copyfile(getPathDBByLang(tempSQLiteDB),getPathDBByLang(lang));
		}
	}

	
	private void copyfile(String fileSrc, String fileDest) {
		try{
			File f1 = new File(fileSrc);
			File f2 = new File(fileDest);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		catch(FileNotFoundException ex){
			Log.error("Impossibile copiare i file db");
		}
		catch(IOException e){
			Log.error("Errore di input/output");
		}
	}


/*
 * 
 * CONNECTIONS
 * 
 */

	private String getPathDBByLang(String lang) {
		return path+File.separator+lang+File.separator+nameSQLiteDB+".db";
	}


	private boolean connectMySQL(){
		//DB Connection
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connMySQL = DriverManager.getConnection("jdbc:mysql://"+HOST+":3306/"+DBNAME+"?user="+USERNAME+"&password="+PASSWORD);
			return true;
		}
		catch (Exception e) {
			Log.error("Error on DB connection: "+e.toString());
			//System.out.println(e);
			return false;
		}
	}
	
	private void disconnectMySQL() {
		try {
			connMySQL.close();
		} catch (SQLException e) {
			Log.error("Error on disconnect MySQL: "+e);
		}
	}

	private Connection connectSQLite(String path) {
		//DB Connection
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			return DriverManager.getConnection("jdbc:sqlite:"+path);
		}
		catch (Exception e) {
			Log.error("Error on DB connection: "+e.toString());
			//System.out.println(e);
			return null;
		}
	}


}
