/**
 * Forl�, 11/ago/2011
 */
package com.mintlab.mx.admin.service.util.dbtranslator.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Mint snc
 * @author Claudio Buda
 */
public class CustomClassLoader extends ClassLoader {

	/**
	 * 
	 */
	public CustomClassLoader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 */
	public CustomClassLoader(ClassLoader parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	public Class loadClass(String classesFolder, String clazz) throws ClassNotFoundException {
		try {
			InputStream input = new FileInputStream(classesFolder + "/" + clazz.replace('.', File.separatorChar) + ".class");
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data = input.read();
			while(data != -1){
				buffer.write(data);
				data = input.read();
			}
			input.close();
			byte[] classData = buffer.toByteArray();
			return defineClass(clazz, classData, 0, classData.length);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		return null;
	}
}
