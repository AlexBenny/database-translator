package com.mintlab.mx.admin.service.util.dbtranslator;

import java.util.ArrayList;
import java.util.Map;

import com.mintlab.mx.admin.service.util.dbtranslator.db.*;
import com.mintlab.mx.admin.service.util.dbtranslator.util.*;
import com.mintlab.mx.admin.service.util.dbtranslator.utils.IDataPublisher;

import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;

import java.sql.*;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;


public class DataPublisher implements IDataPublisher {

	private final static String VERSION = "0.3.10";
	
	public final static String RECORDABLE = "recordable";
	public final static String ACTION_ATTRIBUTE = "action";
	public final static String CHARS_SPECIAL_CONTENT = "__";
	public final static String PARENT_NODE = "__PARENT";
	public final static String NULL_CONTENT = "__NULL";
	public final static String PK_KEY = "DBDestPK";
	public final static String DB_NAME = "data.db";
	public final static String XML_NAME = "data.xml";

	private IWorksheet worksheet;
	private DBManagerAbstract dbDest;
	private ArrayList<DBManagerAbstract> dbsSrc;
	private int maxCycles;
	private int cycleIndex;
	private LocalNodeStorage localStorage;
	private MarkerNodeStorage markerStorage;
	private LocalNodeStorage contentStorage;
	private String workingClassName;
	private Class<IWorksheet> workingClass;

	public DataPublisher() throws MalformedURLException {
		localStorage = new LocalNodeStorage();
		markerStorage = new MarkerNodeStorage();
		contentStorage = new LocalNodeStorage();
	}

	public File[] translate(String worksheet_class, Map<String,Connection> connectionMap, int appPk, String[] languages, String dbsResourcesFolder) {
		workingClassName = worksheet_class;
		ArrayList<File> dbFileList = new ArrayList<File>();
		for (String lang : languages) {
			maxCycles = 1;
			cycleIndex = 0;
			try {
				//creo directory
				String langResourcesPath = dbsResourcesFolder + File.separator + appPk + File.separator + lang + File.separator;
				new File(langResourcesPath).mkdirs();
				String langDbDestPath = langResourcesPath + DB_NAME;

				//inizializzazione
				workingClass = (Class<IWorksheet>) Class.forName(worksheet_class);
				IWorksheet ws = (IWorksheet) workingClass.getConstructors()[0].newInstance();
				ws.init(connectionMap, appPk, lang, dbsResourcesFolder + File.separator + appPk + File.separator);

				//interfaccio ai db src
				dbsSrc = ws.getDbSources();
				for (DBManagerAbstract dbSrc : dbsSrc) {
					dbSrc.open();
				}

				//interfaccio al db dest
				dbDest = ws.getDbDest(langDbDestPath);
				dbDest.open();

				//parsing
				String xml = ws.treeMap();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc;
				try {
					doc = builder.parse(new InputSource(new StringReader(xml)));
				} catch(SAXParseException e) {
					int pos = e.getColumnNumber();
					String xml2 = xml.substring(pos);
					String xml1 = xml.substring(0,pos);
					Log.error("Errore nel parsing xml: "+e.getMessage(), e);
					Log.error(xml1+"<<<<<\r\n\r\n>>>>>"+xml2);
					break;
				}
				// serial actions on doc
				while (maxCycles > 0) {
					// start parsing creation
					parseCreation(doc.getFirstChild());
					//****************************************************************************************************
					//Log.printXML(doc);
					//****************************************************************************************************
					// start parsing recording
					Node node = doc.getFirstChild().getFirstChild(); //devo saltare root
					while (node != null) {
						parseRecording(node);
						node = node.getNextSibling();
					}
					cycleIndex++;
					maxCycles--;
				}

				//disattivo il db src
				for (DBManagerAbstract dbSrc : dbsSrc) {
					dbSrc.close();
				}

				//disattivo il db dest
				dbDest.close();

			
				//salvo il doc xml
				try {
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");

					//initialize StreamResult with File object to save to file
					StreamResult result = new StreamResult(new StringWriter());
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);

					String xmlString = result.getWriter().toString();
					String langXMLDestPath = langResourcesPath + XML_NAME;
					BufferedWriter out = new BufferedWriter(new FileWriter(langXMLDestPath));
					out.write(xmlString);
					out.close();
					
					//registro il path se tutto è andato bene
					dbFileList.add(new File(langDbDestPath));
				}
				catch (IOException e) { Log.error(e.getMessage(), e); }
				catch (TransformerConfigurationException e) { Log.error(e.getMessage(), e); }
				catch (TransformerException e) { Log.error(e.getMessage(), e); }
				catch (TransformerFactoryConfigurationError e) { e.printStackTrace(); }

			}
			catch (InvalidActionException e) {
				Log.error(e.getMessage(), e);
				Log.error(nodeToString(e.getNode()));
			}
			catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
			
		}//end for
		return dbFileList.toArray(new File[dbFileList.size()]);
	}


	private boolean parseCreation(Node node) throws InvalidActionException {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap attrs = node.getAttributes();
			//regolo il numero di cicli di rilettura dell'albero che devo fare
			int numLocActions = 1;
			for (int i=cycleIndex; i<30; i++) {
				if (attrs.getNamedItem(ACTION_ATTRIBUTE+i) != null) numLocActions++;
			}
			if (maxCycles < numLocActions) maxCycles = numLocActions;
			//eseguo l'action
			if (attrs.getNamedItem(ACTION_ATTRIBUTE+cycleIndex) != null) {
				createNodes(node);
				return true;
				/*if (createNodes(node)) return true;*/ // MODIFICA PER ERRORE 30/05/2012
			}
		}
		Node nodeChild = node.getFirstChild();
		while (nodeChild != null) {
			nodeChild = ( parseCreation(nodeChild) ? node.getFirstChild() : nodeChild.getNextSibling() );		}
		return false;
	}


	private boolean createNodes(Node node) throws InvalidActionException {		
		NamedNodeMap attrs = node.getAttributes();
		Node actionAttr = attrs.removeNamedItem(ACTION_ATTRIBUTE+cycleIndex);
		String actionName = actionAttr.getTextContent();
		Log.debug("Execute "+ACTION_ATTRIBUTE+cycleIndex+" - "+actionName);
		Node parent = node.getParentNode();
		try {
			Class actionClass = Class.forName(workingClassName+"$"+actionName);
			//Class actionClass = this.generateClass(workingClassName+"$"+actionName);
			Constructor c = actionClass.getConstructor(workingClass);
			Action action = (Action)c.newInstance(worksheet);
			action.init(localStorage,markerStorage,contentStorage);

			ArrayList<Node> nodeList = action.performAction(dbDest,node);

			parent.removeChild(node);
			for (Node n : nodeList) {
				parent.appendChild(n);
			}
			return !nodeList.isEmpty();
		}
		catch (ClassNotFoundException e) {throw new InvalidActionException("Action "+actionName+" not found \r\n"+e.getMessage(), e);}
		catch (SecurityException e)      {throw new InvalidActionException("Action "+actionName+" security error \r\n"+e.getMessage(), e);}
		catch (NoSuchMethodException e)  {throw new InvalidActionException("Action "+actionName+" invalid \r\n"+e.getMessage(), e);}
		catch (InstantiationException e) {throw new InvalidActionException("Action "+actionName+" invalid \r\n"+e.getMessage(), e);}
		catch (IllegalAccessException e) {throw new InvalidActionException("Action "+actionName+" invalid \r\n"+e.getMessage(), e);}
		catch (InvocationTargetException e) {throw new InvalidActionException("Action "+actionName+" invalid \r\n"+e.getMessage(), e);}
	}


	private void parseRecording(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			recordNode(node);
		}
		node = node.getFirstChild();
		while (node != null) {
			parseRecording(node);
			node = node.getNextSibling();
		}
	}
	private void recordNode(Node node) {
		NamedNodeMap attrs = node.getAttributes();
		//check if recordable
		Node recAttr = attrs.getNamedItem(RECORDABLE);
		if (recAttr != null && recAttr.getTextContent().equals("false")) return;
		String tableName = node.getNodeName();
		//
		Log.debug("Record node - "+tableName);
		//
		String sql = "INSERT INTO "+tableName+" (";
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Object> params = new ArrayList<Object>();
		ArrayList<Integer> typeParams = new ArrayList<Integer>();
		String markerParams = "";
		for (int i=0; i<attrs.getLength(); i++) {
			String nameAttr = attrs.item(i).getNodeName();
			String nameCol = nameAttr.substring(1);
			String value = attrs.item(i).getTextContent();
			if (!nameAttr.startsWith(ACTION_ATTRIBUTE) && !nameAttr.startsWith(RECORDABLE)) {
				if (value.equals(NULL_CONTENT)) {
					continue;
				} else if (value.equals(PARENT_NODE)) {
					params.add(contentStorage.get(node.getParentNode(),CHARS_SPECIAL_CONTENT+PK_KEY));
				} else if (value.startsWith(CHARS_SPECIAL_CONTENT)) {
					Object o = contentStorage.get(node, value);
					if (o == null) continue;
					if (Node.class.isInstance(o)) {
						params.add(contentStorage.get((Node)o,CHARS_SPECIAL_CONTENT+PK_KEY));
					} else {
						params.add((String)o);
					}
				} else {
					params.add(value);
				}
				names.add(nameCol);
				sql += nameCol+",";
				markerParams += "?,";
				String type = nameAttr.substring(0,1);
				if (type.equals("i")) typeParams.add(Integer.valueOf(Types.INTEGER));
				if (type.equals("v")) typeParams.add(Integer.valueOf(Types.VARCHAR));
				if (type.equals("t")) typeParams.add(Integer.valueOf(Types.VARCHAR));
				if (type.equals("f")) typeParams.add(Integer.valueOf(Types.DOUBLE));
			}
		}
		if (names.isEmpty()) return;
		sql =
			sql.substring(0, sql.length()-1) + ") VALUES ("
			+ markerParams.substring(0,markerParams.length()-1) + ")";
//		Log.error(sql+" - "+params.toString());
		if (contentStorage.get(node, CHARS_SPECIAL_CONTENT+PK_KEY)==null && dbDest.doInsert(sql, typeParams, params)) {
			int pk = dbDest.getGeneratedKey();
			contentStorage.put(node, CHARS_SPECIAL_CONTENT+PK_KEY, Integer.valueOf(pk));
		} else {
			//UPDATE
			sql = "UPDATE "+tableName+" SET ";
			for (String name : names) {
				sql += name + "=?, ";
			}
			sql = sql.substring(0,sql.length()-2) + " WHERE pk=" + ((Integer)contentStorage.get(node, CHARS_SPECIAL_CONTENT+PK_KEY)).intValue();
//			Log.error(params.toString()+"  \r\n"+sql);
			if (!dbDest.doInsert(sql, typeParams, params)) {
				Log.error("Errore durante il salvataggio del record: "+sql);
			}
		}
	}
	
	private String nodeToString(Node node) {
		String nodeStatus = "\r\nNode status :\r\n\t";
		NamedNodeMap attrs = node.getAttributes();
		for (int i=0; i<attrs.getLength(); i++) {
			nodeStatus += attrs.item(i).getNodeName()+"='"+attrs.item(i).getTextContent()+"'\r\n\t";
		}
		nodeStatus += "\r\nMarkers: "+markerStorage.toString(node)+
					  "\r\nLocalStorage: "+localStorage.toString(node)+
					  "\r\nContentStorage: "+contentStorage.toString(node);
		return nodeStatus += "\r\n\r\n";
	}


	
	public String version()
	{
		return VERSION;
	}

}
