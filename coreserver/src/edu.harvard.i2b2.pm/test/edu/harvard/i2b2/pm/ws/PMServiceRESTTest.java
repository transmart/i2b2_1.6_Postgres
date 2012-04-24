/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */

package edu.harvard.i2b2.pm.ws;

import static org.junit.Assert.assertNotNull;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.util.PMJAXBUtil;
//import edu.harvard.i2b2.pm.datavo.pm.RequestType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.junit.BeforeClass;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * PM client test. 
 */
public class PMServiceRESTTest extends CRCAxisAbstract { 
	private static EndpointReference targetEPR = new EndpointReference(
	"http://services.i2b2.org/PM/rest/PMService/getServices");

	private static String targetEPR2 =
		"http://127.0.0.1:8080/i2b2/rest/PMService/getServices";
//	"http://services.i2b2.org/PM/rest/PMService/getServices";

	public static OMElement getVersion() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd",
		"tns");

		OMElement method = fac.createOMElement("getServices", omNs);

		return method;
	}

	public static void doPrint(String response) throws Exception {
		JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
				"edu.harvard.i2b2.pm.datavo.pm",
				"edu.harvard.i2b2.pm.datavo.i2b2message"
		});
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
		ResponseMessageType responseMessageType = (ResponseMessageType) jaxbElement.getValue();
		System.out.println("Response Message Number  :" + responseMessageType.getMessageHeader().getMessageControlId().getMessageNum());
	}

	/**
	 * Test code to generate a PM requestPdo for a test sample and convert to
	 * OMElement called by main below
	 *
	 * @param requestPdo
	 *            String requestPdo to send to PM web service
	 * @return An OMElement containing the PM web service requestPdo
	 */
	public static OMElement getPMPayLoad() throws Exception {
		OMElement method = null;

		try {
            StringReader strReader = new StringReader(getPMString());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(strReader);

            StAXOMBuilder builder = new StAXOMBuilder(reader);
            method = builder.getDocumentElement();      			
			
			
			/*
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace("http://www.i2b2.org/xsd/hive/msg/1.1",
			"i2b2");

			method = fac.createOMElement("request", omNs);

			StringReader strReader = new StringReader(getPMString());
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			OMElement lineItem = builder.getDocumentElement();
			method.addChild(lineItem);
			*/
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			// No log because its a thread?
			e.printStackTrace();
			throw new Exception(e);
		}

		return method;
	}

	/**
	 * Test code to generate a PM requestPdo String for a sample PM report
	 * called by main below
	 *
	 * @return A String containing the sample PM report
	 */
	public static String getPMString() throws Exception {
		StringBuffer queryStr = new StringBuffer();
		DataInputStream dataStream = new DataInputStream(PMServiceRESTTest.class.getResourceAsStream(
				"PMSample.xml"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				dataStream));
		String singleLine = null;

		while ((singleLine = reader.readLine()) != null) {
			queryStr.append(singleLine + "\n");
		}

		// Log query string
		//System.out.println("queryStr " + queryStr);

		return queryStr.toString();
	}

	/**
	 * Test code to generate a PM requestPdo based on a sample report and make
	 * a PM web service call PM Response is printed out to console.
	 *
	 */
	private static ConfigureType queryResultInstance = null;
	private  static String testFileDir = null;
	public static void main(String[] args)
	{
		//:TODO accept server url as runtime parameter 
		
		/*
			testFileDir = System.getProperty("testfiledir");
			System.out.println("test file dir " + testFileDir);
			if (!(testFileDir != null && testFileDir.trim().length()>0)) {
				try {
					throw new Exception("please provide test file directory info -Dtestfiledir");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
			//read test file and store query master;
			String filename = "PMSample.xml";
			try { 
			String requestString = getPMString(); //getQueryString(filename);
			System.out.println("test file dir " + testFileDir);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(targetEPR2).sendReceive(requestElement);
			
			//read test file and store query instance ;
			//unmarshall this response string 
			JAXBElement responseJaxb = PMJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
			ConfigureType masterInstanceResult = (ConfigureType)helper.getObjectByClass(r.getMessageBody().getAny(),ConfigureType.class);
			queryResultInstance = masterInstanceResult;
			//assertNotNull(queryResultInstance);
			System.out.println(queryResultInstance);
			} catch (Exception e) { 
				e.printStackTrace();
			}
			//queryResultInstance = new  QueryResultInstanceType();
			//queryResultInstance.setResultInstanceId("4801");
			
			
	}
	public static void main2(String[] args) {
		try {

 			OMElement getPm = getPMPayLoad();

			String requestElementString = getPMString(); //getPm.toString();
			//    System.out.println(requestElementString);
			/* 
			ServicesMessage patientDataMsg = new ServicesMessage(requestElementString);
			 
			PMService pms = new PMService();
			   OMElement pos = pms.getServices(getPm);

			//ServicesType rt = patientDataMsg.getRequestType().getServices();
			SecurityType rmt = patientDataMsg.getRequestMessageType().getMessageHeader().getSecurity();
			
			//log.debug("My username: " + rmt.getUsername());

			System.out.println("My username client: " + rmt.getUsername());
				*/
			Options options = new Options();
			options.setTo(targetEPR);

			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setTimeOutInMilliSeconds(50000);

			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);

			OMElement result = sender.sendReceive(getPm);


			if (result == null) {
				System.out.println("result is null");
			} else {
				String response = result.getFirstElement().toString();
				System.out.println("response = " + response);
				doPrint(response);
			}
		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
