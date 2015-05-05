package com.mintlab.mx.admin.service.util.dbtranslator;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Node;


public interface IAction {

	// Storage locale del nodo
	public Object putLocalStorage(String key, Object value);
	public Object removeLocalStorage(String key);
	public Object getLocalStorage(String key);	

	// Storage locale del nodo genitore
	public Object getParentStorage(String key);	

	// Indicizzatore del nodo all'esterno
	public boolean putMarker(Object marker);  //puoi definire un proprio marker
	public boolean removeMarker(Object marker); //puoi rimuovere solo il tuo marker e non quello altrui
	public Node getMarker(Object marker);	//puoi ottenere qualsiasi nodo dal marker

	// Informazioni specifiche sui contenuti del nodo in db
	public int getPKDest();

	
	
	// ****************** ABSTRACT METHODS **********************
	public abstract IActionSetup setupData(); //torna i parametri per la query
	public abstract boolean behaviour(Data data, Node clone) throws SQLException, Exception; //bool dice se ho fatto bene o male
	// *********************************************************

	
}
