package com.mintlab.mx.admin.service.util.dbtranslator.util;

import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Node;


public final class MarkerNodeStorage {
	
	//BancaDati per le azioni
	private HashMap<Object,Container<Node>> storageObjectKey = new HashMap<Object,Container<Node>>();
	private HashMap<String,Container<Node>> storageStringKey = new HashMap<String,Container<Node>>();
	private HashMap<Node,Container<Node>> containerMap = new HashMap<Node,Container<Node>>();

	
	private Container<Node> insert(Node node) {
		Container<Node> c = containerMap.get(node);
		if (c == null) {
			c = new Container<Node>(node);
			containerMap.put(node, c);
		}
		c.occurency++;
		return c;
	}
	
	private void extract(Node node) {
		Container<Node> c = containerMap.get(node);
		c.occurency--;
		if (c.occurency < 1) {
			containerMap.remove(node);
		}
	}
	
	
	public boolean put(Object key, Node node) {
		if (String.class.isInstance(key)) {
			if (storageStringKey.get((String)key) != null) return false;
			storageStringKey.put((String)key, insert(node));
		} else {
			if (storageObjectKey.get(key) != null) return false;
			storageObjectKey.put(key, insert(node));
		}
		return true;
	}

	public boolean remove(Object key, Node node) {
		if (String.class.isInstance(key)) {
			if (storageStringKey.get((String)key).content == node) {
				storageStringKey.remove((String)key);
				extract(node);
				return true;
			}
		} else {
			if (storageObjectKey.get(key).content == node) {
				storageObjectKey.remove(key);
				extract(node);
				return true;
			}
		}
		return false;
	}

	public Node get(Object key) {
		try {
			if (String.class.isInstance(key)) {
				return storageStringKey.get((String)key).content;
			} else {
				return storageObjectKey.get(key).content;
			}
		} catch (NullPointerException ex) {
			return null;
		}
	}

	
	
	public void substituteNode(Node oldNode, Node newNode) {
		Container<Node> c = containerMap.remove(oldNode);
		if (c==null) return;
		c.content = newNode;
		containerMap.put(newNode, c);
	}
	
	
	public String toString(Node node) {
		String s = "[";
		Container<Node> c = containerMap.get(node);
		if (storageStringKey.containsValue(c)) {
			Set<String> keys = storageStringKey.keySet();
			for (String k : keys) {
				if (storageStringKey.get(k).equals(c)) {
					s += k+", ";
				}
			}
			s = s.substring(0, s.length()-2);
		}
		return s+"]";
	}


	
	
	//INNER CLASS
	private class Container<T> {
		public T content;
		public int occurency = 0;
		
		public Container(T content) {
			this.content = content;
		}
	}
	
}
