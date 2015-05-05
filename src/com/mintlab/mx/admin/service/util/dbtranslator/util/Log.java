package com.mintlab.mx.admin.service.util.dbtranslator.util;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;



public class Log {
	
	private static Document doc;
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(Log.class);
	
	public static void debug(String text) {
		logger.debug(text);
	}

	public static void debug(String text, Exception e) {
		logger.debug(text, e);
	}
	
	public static void error(String text) {
		logger.error(text);
	}

	public static void error(String text, Exception e) {
		logger.error(text, e);
	}
	

	public static void printXML()
	{ 
		printXML(Log.doc); 
	}


	public static void printXML(Document doc) {
		Log.doc = doc;
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			String xmlString = result.getWriter().toString();
			
			logger.debug(xmlString);
			//System.out.println(xmlString);		
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
}
