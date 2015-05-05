package com.mintlab.mx.admin.service.util.dbtranslator;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Node;

import com.mintlab.mx.admin.service.util.dbtranslator.db.*;
import com.mintlab.mx.admin.service.util.dbtranslator.util.*;



public abstract class Action implements IAction  {

	private Node localNodeWorking;
	private Node parentNodeWorking;

	private LocalNodeStorage localStorage;
	private MarkerNodeStorage markerStorage;
	private LocalNodeStorage contentStorage;
	
	void init(LocalNodeStorage localStorage, MarkerNodeStorage markerStorage, LocalNodeStorage contentStorage) {
		this.localStorage = localStorage;
		this.markerStorage = markerStorage;
		this.contentStorage = contentStorage;
	}
	
	ArrayList<Node> performAction(DBManagerAbstract dbDest, Node node) throws InvalidActionException {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		localNodeWorking = node;
		parentNodeWorking = node.getParentNode();
		//*************** CALL SETUP ***********************
		IActionSetup setup = setupData();
		//****************** end ***************************
		if (setup instanceof QuerySetup) {
			QuerySetup qs = (QuerySetup)setup;
			//
			try {
				ResultSet rs = qs.getTargetDB().doQuery(qs.getQuery(),qs.getParamTypes(),qs.getParams());
				Node nodeTemplate = node.cloneNode(true);
				while (rs.next()) {
					Data data = new Data(rs);
					//*************** CALL BEHAVIOUR ***********************
					if (behaviour(data,localNodeWorking)) nodeList.add(localNodeWorking); 
					//********************** end ***************************
					localNodeWorking = nodeTemplate.cloneNode(true);
					localStorage.replicateStorage(node, localNodeWorking);
					contentStorage.replicateStorage(node, localNodeWorking);
					contentStorage.remove(localNodeWorking, DataPublisher.CHARS_SPECIAL_CONTENT+DataPublisher.PK_KEY);
				}
				rs.close();
			} catch(Exception ex) {
				throw new InvalidActionException(localNodeWorking,"Invalid QuerySetup: "+qs.getQuery()+"\r\n"+ex.getMessage(), ex);
			}
		} else if (setup instanceof EmptySetup) {
			try {
				//*************** CALL BEHAVIOUR ***********************
				if (behaviour(null,localNodeWorking)) nodeList.add(localNodeWorking);
				//********************** end ***************************
			} catch(Exception e) {
				throw new InvalidActionException(localNodeWorking,"Invalid EmptySetup behaviour: "+e.getMessage(), e);
			}
		} else if (setup instanceof CustomSetup) {
			CustomSetup ds = (CustomSetup)setup;
			//
			try {
				ArrayList<Data> result = ds.getResult();
				Node nodeTemplate = node.cloneNode(true);
				Iterator<Data> rows = result.iterator();
				while (rows.hasNext()) {
					//*************** CALL BEHAVIOUR ***********************
					if (behaviour(rows.next(),localNodeWorking)) nodeList.add(localNodeWorking); 
					//********************** end ***************************
					localNodeWorking = nodeTemplate.cloneNode(true);
					localStorage.replicateStorage(node, localNodeWorking);
					contentStorage.replicateStorage(node, localNodeWorking);
					contentStorage.remove(localNodeWorking, DataPublisher.CHARS_SPECIAL_CONTENT+DataPublisher.PK_KEY);						
				}
			} catch(Exception ex) {
				throw new InvalidActionException(localNodeWorking,"Invalid CustomSetup \r\n"+ex.getMessage(), ex);
			}
		}
		return nodeList;
	}
	
	public void bindContent(String attributeName, String value) throws InvalidActionException {
		Node attr = localNodeWorking.getAttributes().getNamedItem(attributeName);
		if (attr == null) {
			throw new InvalidActionException(localNodeWorking,"Errore: attributo "+attributeName+" non esiste nel nodo corrente: "+localNodeWorking.getNodeName(), null);
		}
		if (value == null) {
			attr.setTextContent(DataPublisher.NULL_CONTENT);
		}
		attr.setTextContent(value);
	}
	public void bindContent(String attributeName, Node value) throws InvalidActionException { bindContentWithMap(attributeName, value); }
	
	/* @deprecated */
	public void bindContentSecure(String attributeName, String value) throws InvalidActionException { bindContentWithMap(attributeName, value); }
	
	private void bindContentWithMap(String attributeName, Object value) throws InvalidActionException {
		Node attr = localNodeWorking.getAttributes().getNamedItem(attributeName);
		if (attr == null) {
			throw new InvalidActionException(localNodeWorking,"Errore: attributo "+attributeName+" non esiste nel nodo corrente: "+localNodeWorking.getNodeName(), null);
		}
		if (value == null) {
			attr.setTextContent("__NULL");
		} else {
			contentStorage.put(localNodeWorking, DataPublisher.CHARS_SPECIAL_CONTENT+attributeName+localNodeWorking.hashCode(), value);
			attr.setTextContent(DataPublisher.CHARS_SPECIAL_CONTENT+attributeName+localNodeWorking.hashCode());
		}
	}


	//************** Storage Locale ******************************
	public final Object putLocalStorage(String key, Object value) {
		return localStorage.put(localNodeWorking, key, value);
	}
	public final Object removeLocalStorage(String key) {
		return localStorage.remove(localNodeWorking, key);
	}
	public final Object getLocalStorage(String key) {
		return localStorage.get(localNodeWorking, key);
	}

	//************** Storage Genitore ******************************
	public final Object getParentStorage(String key) {
		Node n = parentNodeWorking;
		Object varValue = localStorage.get(n, key);
		while (varValue == null) {
			n = n.getParentNode();
			if (n==null) return null;
			varValue = localStorage.get(n, key);
		}
		return varValue;
	}

	//************** Storage Markers *****************************
	public boolean putMarker(Object marker) {
		return markerStorage.put(marker,localNodeWorking);
	}
	public boolean removeMarker(Object marker) {
		return markerStorage.remove(marker,localNodeWorking);		
	}
	public Node getMarker(Object marker) {
		return markerStorage.get(marker);		
	}

	//*********************** Content *****************************
	public int getPKDest() {
		return ((Integer)contentStorage.get(localNodeWorking, DataPublisher.CHARS_SPECIAL_CONTENT+DataPublisher.PK_KEY)).intValue();
	}

	
	// ****************** ABSTRACT METHODS **********************
	public abstract IActionSetup setupData(); //torna i parametri per la query
	public abstract boolean behaviour(Data data, Node clone) throws Exception; //bool dice se mantenere il nodo o toglierlo
	// *********************************************************
	
}