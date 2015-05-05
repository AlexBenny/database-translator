package com.mintlab.mx.admin.service.util.dbtranslator.util;

import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Node;



public final class LocalNodeStorage {

	//BancaDati per le actions
	private HashMap<Node,HashMap<String,Object>> storageData
	= new HashMap<Node,HashMap<String,Object>>();


	//Metodi storage Data
	private HashMap<String,Object> get(Node node) {
		HashMap<String,Object> map = storageData.get(node);
		if (map == null) {
			map = new HashMap<String,Object>();
			storageData.put(node, map);
		}
		return map;
	}


	public Object put(Node node, String key, Object value) {
		return get(node).put(key, value);
	}

	public Object remove(Node node, String key) {
		return get(node).remove(key);
	}

	public Object get(Node node, String key) {
		return get(node).get(key);
	}


	
	public void replicateStorage(Node oldNode, Node newNode) {
		HashMap<String,Object> map = storageData.get(oldNode);
		if (map != null) {
			HashMap<String,Object> mapRepl = (HashMap<String, Object>) map.clone();
			storageData.put(newNode, mapRepl);
		}
	}
	
	
	public String toString(Node node) {
		HashMap<String,Object> map = get(node);
		return map.toString();
	}
	
}
