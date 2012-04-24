/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.pm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.pm.util.PMJAXBUtil;

/**
 * Class to test different pdo requests 
 * @author rkuttan
 */
public class PdoQueryTest extends CRCAxisAbstract {

	
	private static ConfigureType queryResultInstance = null;
	private  static String testFileDir = null;
	//:TODO accept server url as runtime parameter 
	private static String setfinderTargetEPR = 
			"http://localhost:8080/i2b2/rest/QueryToolService/request";			

	private static String pdoTargetEPR = 
			"http://localhost:8080/i2b2/rest/QueryToolService/pdorequest";			
	
	
	
	@BeforeClass public  static void runQueryInstanceFromQueryDefinition() throws Exception  {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);
		if (!(testFileDir != null && testFileDir.trim().length()>0)) {
			throw new Exception("please provide test file directory info -Dtestfiledir");
		}
		//read test file and store query master;
		String filename = testFileDir +"/PMSample.xml";
		try { 
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderTargetEPR).sendReceive(requestElement);
		
		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		ConfigureType masterInstanceResult = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
		queryResultInstance = masterInstanceResult;
		assertNotNull(queryResultInstance);
		System.out.println(queryResultInstance);
		} catch (Exception e) { 
			e.printStackTrace();
			throw e;
		}
		//queryResultInstance = new  QueryResultInstanceType();
		//queryResultInstance.setResultInstanceId("4801");
		
		
	}
	
	

	
	
}
