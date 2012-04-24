/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.fr.datavo.FRJAXBUtil;
import edu.harvard.i2b2.fr.datavo.fr.query.RecvfileResponseType;
import edu.harvard.i2b2.fr.datavo.i2b2message.ResponseMessageType;


/**
 * This class test pdo/timeline requests.
 * @author rk903
 */
public class CRCLoaderQueryClientTest extends CRCAxisAbstract {

	private static String serviceUrl = "http://phsi2b2appdev:8080/i2b2/services/FRService";
	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = "/Users/mem61"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
			"please provide test file directory info -Dtestfiledir");
		}
	}


	@Ignore
	@Test
	public void testPDOMessage() throws Exception {
		String filename = testFileDir + "/i2b2_publish_data.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		String publishUrl = serviceUrl + "/recvfileRequest";

		OMElement requestElement = convertStringToOMElement(requestString);
		OMElement responseElement = getServiceClient(publishUrl)
		.sendReceive(requestElement);

		System.out.println("Response message " + responseElement.toString());
		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = FRJAXBUtil.getJAXBUtil()
		.unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'", "DONE",
				r.getResponseHeader().getResultStatus().getStatus().getType());

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		RecvfileResponseType loadDataResponse = (RecvfileResponseType) helper.getObjectByClass(r.getMessageBody()
				.getAny(),
				RecvfileResponseType.class);
		assertNotNull("checking uploadid not null ",
				loadDataResponse.getRecvfileResponse());

		assertNotNull("checking load status",
				loadDataResponse.getRecvfileResponse());
	}


	@Test
	public void testGetUploadInfoMessage() throws Exception {
		String filename = testFileDir + "/i2b2_send_data.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		String getStatusUrl = serviceUrl + "/sendfileRequest";
		String attachmentfilename = "/Users/mem61/iamhere.txt";
		javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(attachmentfilename));

		
		
		/*
		
		
		Options options = new Options();
		options.setTo( new EndpointReference(serviceUrl));
		options.setProperty(Constants.Configuration.ENABLE_SWA,
				Constants.VALUE_TRUE);
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		// Increase the time out when sending large attachments
		options.setTimeOutInMilliSeconds(10000);
		options.setTo( new EndpointReference(serviceUrl));
		options.setAction("urn:sendfileRequest");

		// assume the use runs this sample at
		// <axis2home>/samples/soapwithattachments/ dir
		ConfigurationContext configContext = ConfigurationContextFactory
				.createConfigurationContextFromFileSystem("../../repository",
						null);

		ServiceClient sender = new ServiceClient(configContext, null);
		sender.setOptions(options);
		OperationClient mepClient = sender
				.createClient(ServiceClient.ANON_OUT_IN_OP);

		MessageContext mc = new MessageContext();

		// Create a dataHandler using the fileDataSource. Any implementation of
		// javax.activation.DataSource interface can fit here.

		String attachmentID = mc.addAttachment(dataHandler);

		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope env = fac.getDefaultEnvelope();
		OMNamespace omNs = fac.createOMNamespace(
				"http://service.soapwithattachments.sample/xsd", "swa");
		OMElement uploadFile = fac.createOMElement("uploadFile", omNs);
		OMElement nameEle = fac.createOMElement("name", omNs);
		nameEle.setText(attachmentfilename);
		OMElement idEle = fac.createOMElement("attchmentID", omNs);
		idEle.setText(attachmentID);
		uploadFile.addChild(nameEle);
		uploadFile.addChild(idEle);
		env.getBody().addChild(uploadFile);
		mc.setEnvelope(env);
		mepClient.addMessageContext(mc);
		mepClient.execute(true);
		MessageContext response = mepClient
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
	  	SOAPBody body = response.getEnvelope().getBody();
	  	OMElement element = body.getFirstElement().getFirstChildWithName(new QName("return"));
		System.out.println(element.getText());
		*/
		
		
//IM AMHERE

		Options options = new Options();
		options.setTo( new EndpointReference(serviceUrl));
		options.setAction("urn:sendfileRequest");
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);


		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(10000);

		options.setProperty(Constants.Configuration.ENABLE_SWA,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,"temp");
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");

		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

		MessageContext mc = new MessageContext();

		mc.addAttachment("cid",dataHandler);
		mc.setDoingSwA(true);

		OMElement requestElement = convertStringToOMElement(requestString);
		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope env = sfac.getDefaultEnvelope();
		
		/*OMNamespace omNs = sfac.createOMNamespace(
				"http://www.i2b2.org/xsd", "swa");
		OMElement idEle = sfac.createOMElement("attchmentID", omNs);
		idEle.setText("cid");
		
		env.getBody().addChild(idEle);
		*/
		env.getBody().addChild(requestElement);

		System.out.println("NEW REQUEST: " + env.toString());
		// SOAPEnvelope env = createEnvelope("fileattachment");
		mc.setEnvelope(env);
		mepClient.addMessageContext(mc);
		mepClient.execute(true);


		System.out.println("attachment length: " + mc.getAttachmentMap().getAllContentIDs().length);

		MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		SOAPBody body = response.getEnvelope().getBody();



		Attachments attach = response.getAttachmentMap(); //
//		String[] ids = attach.getAllContentIDs();

		javax.activation.DataHandler dataHandler2 = response.getAttachment("cid");
		if (dataHandler2!=null){
			// Writing the attachment data (graph image) to a file
			File graphFile = new File("/tmp/responseGraph.png");
			FileOutputStream outputStream = new FileOutputStream(graphFile);
			dataHandler2.writeTo(outputStream);
			outputStream.flush();
			System.out.println("Download statistics graph saved to :" + graphFile.getAbsolutePath());
		}else
		{
			throw new Exception("Cannot find the data handler.");
		}



		OMElement element = body.getFirstChildWithName(new QName("http://www.i2b2.org/xsd","getStatsResponse"));
		if (element!=null)
		{
			OMElement graphElement = element.getFirstChildWithName(new QName("http://www.i2b2.org/xsd","graph"));
			//retrieving the ID of the attachment
			String graphImageID = graphElement.getAttributeValue(new QName("href"));
			//remove the "cid:" prefix
			graphImageID = graphImageID.substring(4);


			dataHandler2 = response.getAttachment(graphImageID);
			if (dataHandler!=null){
				// Writing the attachment data (graph image) to a file
				File graphFile = new File("/tmp/responseGraph.png");
				FileOutputStream outputStream = new FileOutputStream(graphFile);
				dataHandler2.writeTo(outputStream);
				outputStream.flush();
				System.out.println("Download statistics graph saved to :" + graphFile.getAbsolutePath());
			}else
			{
				throw new Exception("Cannot find the data handler.");
			}

			//processResponse(response, element);
		}else{
			System.out.println("Malformed response.");
		}



		OMElement responseElement = getServiceClient(getStatusUrl)
		.sendReceive(requestElement);
		System.out.println("Response message " + responseElement.toString());




		/* orig

		OMElement requestElement = convertStringToOMElement(requestString);
		ServiceClient serviceClient = getServiceClient(serviceUrl);
		OperationClient op = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);



		MessageContext mc = new MessageContext();
		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope se = sfac.getDefaultEnvelope();
		se.getBody().addChild(requestElement);
		mc.setEnvelope(se);

		mc.addAttachment("fileattachment",dataHandler);
		//   mc.addAttachment("f1", dataHandler);
		//  mc.addAttachment(dataHandler);


		mc.setDoingSwA(true);

		op.addMessageContext(mc); 

		System.out.println("attachment length" + mc.getAttachmentMap().getAllContentIDs().length);
		op.execute(true);

		 */

		/*
        OMElement requestElement = convertStringToOMElement(requestString);
        OMElement responseElement = getServiceClient(getStatusUrl)
                                                .sendReceive(requestElement);

        System.out.println("Response message " + responseElement.toString());
        //read test file and store query instance ;
        //unmarshall this response string 
        JAXBElement responseJaxb = FRJAXBUtil.getJAXBUtil()
                                              .unMashallFromString(responseElement.toString());


        ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
        assertEquals("checking i2b2 message status 'DONE'", "DONE",
            r.getResponseHeader().getResultStatus().getStatus().getType());

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        RecvfileResponseType loadDataListResponse = (RecvfileResponseType) helper.getObjectByClass(r.getMessageBody()
                                                                                                             .getAny(),
                                                                                                             RecvfileResponseType.class);
        assertNotNull("checking uploadid not null ",
            loadDataListResponse.getRecvfileResponse().getDate());

        assertNotNull("checking load status",
            loadDataListResponse.getRecvfileResponse().getHash());

		 */
	}

	@Test
	public void testGetDownloadInfoMessage() throws Exception {
		String filename = testFileDir + "/i2b2_publish_data.xml";
		String requestString = getQueryString(filename);
		System.out.println("test file dir " + testFileDir);
		String getStatusUrl = serviceUrl + "/recvfileRequest";


		String attachmentfilename = "/Users/mem61/iamhere.txt";

		javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(attachmentfilename));



		Options options = new Options();
		options.setTo( new EndpointReference(serviceUrl));
		options.setAction("urn:recvfileRequest");
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);


		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(10000);


		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,"temp");
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");

		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

		MessageContext mc = new MessageContext();


		OMElement requestElement = convertStringToOMElement(requestString);
		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope env = sfac.getDefaultEnvelope();
		env.getBody().addChild(requestElement);

		// SOAPEnvelope env = createEnvelope("fileattachment");
		mc.setEnvelope(env);
		//mc.addAttachment("contentID",dataHandler);
		mc.setDoingSwA(true);
		mepClient.addMessageContext(mc);
		mepClient.execute(true);


		//System.out.println("attachment length: " + mc.getAttachmentMap().getAllContentIDs().length);

		MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		SOAPBody body = response.getEnvelope().getBody();



		Attachments attach = response.getAttachmentMap(); //
//		String[] ids = attach.getAllContentIDs();

		if (attach!=null){
			javax.activation.DataHandler dataHandler2 = response.getAttachment("cid");
			// Writing the attachment data (graph image) to a file
			File graphFile = new File("/tmp/responseGraph.png");
			FileOutputStream outputStream = new FileOutputStream(graphFile);
			dataHandler2.writeTo(outputStream);
			outputStream.flush();
			System.out.println("Download statistics graph saved to :" + graphFile.getAbsolutePath());
		}else
		{
			throw new Exception("Cannot find the data handler.");
		}



		OMElement element = body.getFirstChildWithName(new QName("http://www.i2b2.org/xsd","getStatsResponse"));
		if (element!=null)
		{
			OMElement graphElement = element.getFirstChildWithName(new QName("http://www.i2b2.org/xsd","graph"));
			//retrieving the ID of the attachment
			String graphImageID = graphElement.getAttributeValue(new QName("href"));
			//remove the "cid:" prefix
			graphImageID = graphImageID.substring(4);


			DataHandler dataHandler2 = response.getAttachment(graphImageID);
			if (dataHandler!=null){
				// Writing the attachment data (graph image) to a file
				File graphFile = new File("/tmp/responseGraph.png");
				FileOutputStream outputStream = new FileOutputStream(graphFile);
				dataHandler2.writeTo(outputStream);
				outputStream.flush();
				System.out.println("Download statistics graph saved to :" + graphFile.getAbsolutePath());
			}else
			{
				throw new Exception("Cannot find the data handler.");
			}

			/*
    		Attachments attach = response.getAttachmentMap(); //
    		String[] ids = attach.getAllContentIDs();
    		if (ids!=null){
    			javax.activation.DataHandler dataHandler2 = response.getAttachment(ids[1]);
    			// Writing the attachment data (graph image) to a file
    			File graphFile = new File("/tmp/responseGraph.png");
    			FileOutputStream outputStream = new FileOutputStream(graphFile);
    			dataHandler2.writeTo(outputStream);
    			outputStream.flush();
    			System.out.println("Download statistics graph saved to :" + graphFile.getAbsolutePath());
    		}else
    		{
    			throw new Exception("Cannot find the data handler.");
    		}
			 */
			//processResponse(response, element);
		}else{
			System.out.println("Malformed response.");
		}



		OMElement responseElement = getServiceClient(getStatusUrl)
		.sendReceive(requestElement);
		System.out.println("Response message " + responseElement.toString());

		/* orig

		OMElement requestElement = convertStringToOMElement(requestString);
		ServiceClient serviceClient = getServiceClient(serviceUrl);
		OperationClient op = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);



		MessageContext mc = new MessageContext();
		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope se = sfac.getDefaultEnvelope();
		se.getBody().addChild(requestElement);
		mc.setEnvelope(se);

		mc.addAttachment("fileattachment",dataHandler);
		//   mc.addAttachment("f1", dataHandler);
		//  mc.addAttachment(dataHandler);


		mc.setDoingSwA(true);

		op.addMessageContext(mc); 

		System.out.println("attachment length" + mc.getAttachmentMap().getAllContentIDs().length);
		op.execute(true);

		 */

		/*
        OMElement requestElement = convertStringToOMElement(requestString);
        OMElement responseElement = getServiceClient(getStatusUrl)
                                                .sendReceive(requestElement);

        System.out.println("Response message " + responseElement.toString());
        //read test file and store query instance ;
        //unmarshall this response string 
        JAXBElement responseJaxb = FRJAXBUtil.getJAXBUtil()
                                              .unMashallFromString(responseElement.toString());


        ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
        assertEquals("checking i2b2 message status 'DONE'", "DONE",
            r.getResponseHeader().getResultStatus().getStatus().getType());

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        RecvfileResponseType loadDataListResponse = (RecvfileResponseType) helper.getObjectByClass(r.getMessageBody()
                                                                                                             .getAny(),
                                                                                                             RecvfileResponseType.class);
        assertNotNull("checking uploadid not null ",
            loadDataListResponse.getRecvfileResponse().getDate());

        assertNotNull("checking load status",
            loadDataListResponse.getRecvfileResponse().getHash());

		 */
	}	
	

	private static SOAPEnvelope createEnvelope(String destinationFile) {
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope env = fac.getDefaultEnvelope();
		OMNamespace omNs = fac.createOMNamespace("http://service.sample/xsd",
		"swa");
		OMElement statsElement = fac.createOMElement("recvfileRequest", omNs);
		OMElement nameEle = fac.createOMElement("projectName", omNs);
		nameEle.setText(destinationFile);
		statsElement.addChild(nameEle);
		env.getBody().addChild(statsElement);
		return env;
	}

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(CRCLoaderQueryClientTest.class);
	}

}
