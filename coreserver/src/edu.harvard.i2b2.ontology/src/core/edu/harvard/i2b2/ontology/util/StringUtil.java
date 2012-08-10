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
    
}