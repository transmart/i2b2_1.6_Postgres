/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

/**
 * This class test pdo/timeline requests.
 * 
 * @author rk903
 */
public class CRCLoaderQueryClientTest extends CRCAxisAbstract {

	// private static String serviceUrl =
	// "http://infra3:9090/i2b2/rest/CRCLoaderService";
	// for attachment
	// private static String serviceUrl =
	// "http://localhost:9090/i2b2/services/CRCLoaderService";
	private static String serviceUrl = "http://localhost:8080/i2b2/rest/CRCLoaderService";
	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	@Ignore
	@Test
	public void testAttachment() throws Exception {
		String filename = testFileDir + "/i2b2_publish_data.xml";
		// String attachmentfilename =
		// "/Users/rk903/Desktop/jboss-4.2.2.GA-src.tar.gz";
		String attachmentfilename = testFileDir + "/i2b2_publish_data.xml"; // "/crcapp/jdbc.properties"
		// ;
		// String filename = testFileDir + "/mike_test.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);

		String publishUrl = serviceUrl + "/mtomSample";

		//options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)
		// ;
		OMElement requestElement = convertStringToOMElement(requestString);
		javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(
				new FileDataSource(attachmentfilename));

		// OMFactory fac = OMAbstractFactory.getOMFactory();
		// OMText textData = fac.createOMText(dataHandler,true);

		// requestElement.addChild(textData);
		// OMElement requestElement = fac.createOMElement("request", null);
		// textData = fac.createOMText("arg0");
		// OMElement firstElement = fac.createOMElement("firstelement", null);
		// firstElement.addChild(requestElement);
		// firstElement.addChild(textData);

		System.out.println("first element " + requestElement.getFirstElement());
		System.out.println("first element omchild"
				+ requestElement.getFirstElement().getFirstOMChild());

		requestElement = convertStringToOMElement(requestString);
		ServiceClient serviceClient = getServiceClient(publishUrl);
		OperationClient op = serviceClient
				.createClient(ServiceClient.ANON_OUT_IN_OP);

		// System.out.println("request element" + requestElement);
		MessageContext mc = new MessageContext();

		// mc.setDoingREST(true);

		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope se = sfac.getDefaultEnvelope();
		se.getBody().addChild(requestElement);
		mc.setEnvelope(se);

		mc.addAttachment("fileattachment", dataHandler);
		// mc.addAttachment("f1", dataHandler);
		// mc.addAttachment(dataHandler);
		mc.setDoingSwA(true);
		// mc.getAttachmentMap().addDataHandler("fileattachment", dataHandler);
		op.addMessageContext(mc);

		System.out.println("attachment length"
				+ mc.getAttachmentMap().getAllContentIDs().length);
		op.execute(true);

		OperationContext operationContext = mc.getOperationContext();
		MessageContext outMessageContext = operationContext
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		System.out
				.println(" length"
						+ outMessageContext.getAttachmentMap()
								.getAllContentIDs().length);
		DataHandler outDataHandler = outMessageContext.getAttachment("f1");
		if (outDataHandler != null) {
			System.out.println("good not null");
		} else {
			System.out
					.println("bad null"
							+ " "
							+ outMessageContext.getAttachmentMap()
									.getAllContentIDs()[0]);
			;
		}

		// javax.activation.DataHandler outDataHandler = new
		// javax.activation.DataHandler(new FileDataSource(attachmentfilename));

		// OMElement responseElement =
		// getServiceClient(publishUrl).sendReceive(firstElement);
		// System.out.println(responseElement);
	}

	@Test
	public void testUploadMessage() throws Exception {
		String filename = testFileDir + "/i2b2_publish_data.xml";
		// String filename = testFileDir + "/mike_test.xml";
		// String filename = testFileDir + "/pdo.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		String publishUrl = serviceUrl + "/publishDataRequest";

		OMElement requestElement = convertStringToOMElement(requestString);
		System.out.println(requestElement);
		OMElement responseElement = getRestServiceClient(publishUrl)
				.sendReceive(requestElement);

		System.out.println("Response message " + responseElement.toString());
		// read test file and store query instance ;
		// unmarshall this response string
		JAXBElement responseJaxb = CRCLoaderJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'", "DONE", r
				.getResponseHeader().getResultStatus().getStatus().getType());

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		LoadDataResponseType loadDataResponse = (LoadDataResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						LoadDataResponseType.class);
		assertNotNull("checking uploadid not null ", loadDataResponse
				.getUploadId());

		assertNotNull("checking load status", loadDataResponse.getLoadStatus());
	}

	@Ignore
	@Test
	public void testGetUploadInfoMessage() throws Exception {
		String filename = testFileDir + "/i2b2_get_load_status.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		String getStatusUrl = serviceUrl + "/getLoadDataStatusRequest";
		OMElement requestElement = convertStringToOMElement(requestString);
		OMElement responseElement = getRestServiceClient(getStatusUrl)
				.sendReceive(requestElement);

		System.out.println("Response message " + responseElement.toString());
		// read test file and store query instance ;
		// unmarshall this response string
		JAXBElement responseJaxb = CRCLoaderJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'", "DONE", r
				.getResponseHeader().getResultStatus().getStatus().getType());

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		LoadDataListResponseType loadDataListResponse = (LoadDataListResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						LoadDataListResponseType.class);
		assertNotNull("checking uploadid not null ", loadDataListResponse
				.getLoadDataResponse().get(0).getUploadId());

		assertNotNull("checking load status", loadDataListResponse
				.getLoadDataResponse().get(0).getLoadStatus());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CRCLoaderQueryClientTest.class);
	}

}
