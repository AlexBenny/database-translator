package com.mintlab.mx.admin.service.util.dbtranslator;

import org.w3c.dom.Node;

public class InvalidActionException extends Exception {
	
	private Node node;
	
	public InvalidActionException(String message, Exception e) {
		super(message,e);
	}

	public InvalidActionException(Node node, String message, Exception e) {
		super(message,e);
		this.node = node;
	}
	
	public Node getNode() {
		return node;
	}

}
