package com.mintlab.dbconverter.engine;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import com.mintlab.dbconverter.ColumnDescriptor;
import com.mintlab.dbconverter.Constraint;
import com.mintlab.dbconverter.ConstraintLang;
import com.mintlab.dbconverter.Log;
import com.mintlab.dbconverter.Relation;
import com.mintlab.dbconverter.TableDescriptor;
import com.mintlab.dbconverter.TablesChain;


public class Engine {


	public static void populateDB(Connection srcDBconnection, Connection destDBconnection, Collection<TableDescriptor> tables, Collection<Constraint> constraints) {
		for (TableDescriptor td : tables) {
			//
			LinkedList<TablesChain> listChains = new LinkedList<TablesChain>();
			for (Constraint cnst : constraints) {
				TablesChain tchain = new TablesChain();
				tchain.setConstraint(cnst);
				tchain.setFocusTable(td);
				//Check se constraint = lang per semplificare il sistema
				if (ConstraintLang.class.isInstance(cnst)) {
					for (Relation r : td.getRelationsN1()) {
						TableDescriptor tempTd = r.getTableDestDescriptor();
						if (tempTd.has(cnst.getColumnName())) {
							tchain.add(r);
							break;
						}
					}
					if (!tchain.isEmpty()) {
						listChains.add(tchain);
					}
				} else {
					//Gestione altri constraints
					explore(td,cnst,tchain);
					listChains.add(tchain);
				}	
			}
			Log.error(listChains.toString());
			//
			String sCols = "  ";
			for (ColumnDescriptor cd : td.getColumns()) {
				sCols += td.getName() + (cd.isImported() ? "_translation" : "") + "." + cd.getName() + ", ";
			}
			sCols = sCols.substring(0, sCols.length()-2);
			//
			String sFrom = " ";
			String sWhere = "    ";
			for (TablesChain chain : listChains) {
				TableDescriptor precTdc = chain.getFocusTable();
				for (Relation rel : chain) {
					TableDescriptor tdc = rel.getTableSrcDescriptor();
					if (tdc == null) break; //Raggiunto ultimo elemento chain
					String tName = tdc.getName();
					if (precTdc != null) {
						if (rel.getTableDestDescriptor() != null) {
							sFrom +=
								rel.getTableSrcDescriptor().getName()+" JOIN "+
								rel.getTableDestDescriptor().getName()+" ON "+
								rel.getSQLRelation()+", ";
						} else {
							sFrom += rel.getTableSrcDescriptor().getName()+", ";
							sWhere += rel.getSQLRelation()+" AND ";
						}
					}
					//rootTdc = precTdc;
					precTdc = tdc;
				}
			}
			sFrom = sFrom.substring(0, sFrom.length()-1);
			sWhere = sWhere.substring(0, sWhere.length()-4);
			//
			String query = "SELECT "+sCols+" FROM "+sFrom+" WHERE "+sWhere;
			Log.error(query);
			try {
				PreparedStatement pstatSrcDB = srcDBconnection.prepareStatement(query);
				ResultSet rs = pstatSrcDB.executeQuery();
				//
				sCols = "  ";
				for (ColumnDescriptor cd : td.getColumns()) {
					sCols += cd.getName() + ", ";
				}
				sCols = sCols.substring(0, sCols.length()-2);
				while (rs.next()) {
					try {
						query = "INSERT INTO "+td.getName()+" ("+sCols+") VALUES (";
						for (int i=0; i<td.getColumns().size(); i++) { query += "?,"; }
						query = query.substring(0,query.length()-1) + ")";
						Log.error(query);
						PreparedStatement pstatSQLite = destDBconnection.prepareStatement(query);
						int i=1;
						for (ColumnDescriptor cd : td.getColumns()) {
							if (cd.getDataType().startsWith("varchar")) {
								pstatSQLite.setString(i++, rs.getString(cd.getName()) );
								Log.error(""+rs.getString(cd.getName()));
							} else if (cd.getDataType().startsWith("int")) {
								pstatSQLite.setInt(i++, rs.getInt(cd.getName()) );
								Log.error(""+rs.getInt(cd.getName()));
							} else if (cd.getDataType().startsWith("datetime")) {
								Timestamp date = rs.getTimestamp(cd.getName());
								String dt = "";
								if (date != null) {
									String dtOld = date.toString();
									dt = dtOld.substring(0,4) + "-"
									+ dtOld.substring(5,7) + "-"
									+ dtOld.substring(8,10) + " "
									+ dtOld.substring(11,13) + ":"
									+ dtOld.substring(14,16) + ":"
									+ dtOld.substring(17,19);
								}
								pstatSQLite.setString(i++, dt);
								Log.error(""+rs.getTimestamp(cd.getName()));
							} else if (cd.getDataType().startsWith("double")) {
								pstatSQLite.setDouble(i++, rs.getDouble(cd.getName()) );
								Log.error(""+rs.getDouble(cd.getName()));
							}
						}
						pstatSQLite.execute();
					} catch(SQLException e) {
						Log.error("Error writing record: "+e);
					}
				}
			} catch(SQLException e) {
				Log.error("Error select records: "+e);
			}
		}
	}


	private static boolean explore(TableDescriptor td, Constraint cnst, TablesChain tchain) {
		ColumnDescriptor cnstCD = cnst.getColumnDescriptor();
		if (cnstCD==null ? td.has(cnst.getColumnName()) : td.has(cnstCD)) {
			tchain.add(cnst.getRelationCnst(td, Relation.RELATION_1N));
			return true;
		} else {
			for (Relation r : td.getRelations1N()) {
				TableDescriptor td2 = r.getTableDestDescriptor();
				if (!tchain.contains(td2)) {
					int marker = tchain.size();
					tchain.add(r);
					if (explore(td2, cnst, tchain)) {
						return true;
					} else {
						while (tchain.size() != marker) tchain.removeLast();
					}
				}
			}
			for (Relation r : td.getRelationsN1()) {
				TableDescriptor td2 = r.getTableSrcDescriptor();
				if (!tchain.contains(td2)) {
					int marker = tchain.size();
					tchain.add(r);
					if (explore(td2, cnst, tchain)) {
						return true;
					} else {
						while (tchain.size() != marker) tchain.removeLast();
					}
				}
			}
			return false;
		}
	}


}
