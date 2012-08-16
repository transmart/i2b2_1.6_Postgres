/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;



/**
 * StringUtil class to perform string parsing tasks
 * This is singleton class.
 * @author lcp5
 */
public class StringUtil {
	private static Log log = LogFactory.getLog(StringUtil.class.getName());
    //to make this class singleton
    private static StringUtil thisInstance;
    
    static {
            thisInstance = new StringUtil();
    }
    
    public static StringUtil getInstance() {
        return thisInstance;
    }
    
    public static String getTableCd(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);
    	return fullPath.substring(2, end).trim();
    }
    
    public static String getPath(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);

    	fullPath = fullPath.substring(end).trim();
    	
    	Boolean addDelimiter = true;
    	
    	try {
			addDelimiter = OntologyUtil.getInstance().getOntTerminalDelimiter();
		} catch (I2B2Exception e) {
			log.debug(e.getMessage() + " property set to false");
			addDelimiter = false;  //if delimiter property is missing; assume false.
			
		}
    	
		// add trailing backslash if ont.terminal.delimiter property set to true.
		if(addDelimiter) {
			if (fullPath.lastIndexOf('\\') != fullPath.length()-1) {
				fullPath = fullPath + "\\";
			}
		}
    	return fullPath;
    	
    	
    }
    
    public static String getLiteralPath(String fullPath) {
    	int end = fullPath.indexOf("\\", 3);

    	fullPath = fullPath.substring(end).trim();
    	
    	return fullPath;
    	    	
    }
    
    // smuniraju: Used with strings that already have '\' escaped once through 
	// functions like getLiteralPath()  
    public static String escapeBackslash(String input, String serverType) {		
		String output = null;
		
		if(input == null) return output;
		
		if (serverType.equals("POSTGRES")) {
			
			//Postgres treats backslash as an escape character in search expressions
			//escape the escape characters.
			output = input.replaceAll("\\\\", "\\\\\\\\"); 	
		} else {
			output = input ; 
		}
		
		return output;		
	}
    
    // smuniraju: Postgres requires escaping a '\' twice while performing a search
	public static String doubleEscapeBackslash(String input, String serverType) {		
		String output = null;
		
		if(input == null) return output;
		
		if (serverType.equals("POSTGRES")) {
			
			//Postgres treats backslash as an escape character in search expressions
			//escape the escape characters.
			output = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");  			
		} else {
			output = input ; 
		}
		
		return output;		
	}
	
	/**
	 * 
	 * Use this function to escape single quote string
	 * For example: Hi' Hello --> Hi'' Hello
	 * @param value string 
	 * @return single quote escaped string
	 */
	public static String escapeSingleQuote(String value) { 
		String escapedValue = null;
		if (value != null) { 
			escapedValue = value.replaceAll("'", "\\''");
		}
		return escapedValue;
	}
}
