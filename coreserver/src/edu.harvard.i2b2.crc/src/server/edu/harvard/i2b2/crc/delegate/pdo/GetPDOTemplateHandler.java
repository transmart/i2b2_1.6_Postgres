/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.pdo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOTemplateRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * GetPDOFromInputListHandler class. $Id: GetPDOFromInputListHandler.java,v 1.10
 * 2008/07/21 19:56:56 rk903 Exp $
 * 
 * @author rkuttan
 */
public class GetPDOTemplateHandler extends RequestHandler {
	private GetPDOTemplateRequestType getPDOTemplateRequestType = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GetPDOTemplateHandler(String requestXml) throws I2B2Exception {
		try {
			getPDOTemplateRequestType = (GetPDOTemplateRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOTemplateRequestType.class);
			setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 */
	public BodyType execute() throws I2B2Exception {
		// call ejb and pass input object
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String responseString = null;
		BodyType bodyType = new BodyType();

		try {
			InputStream resourceAsStream = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							"pdo_template.xml");

			// .currentThread()
			// .getContextClassLoader()
			// .getResourceAsStream(
			// );
			String pdoXmlContent = convertStreamToString(resourceAsStream);
			JAXBElement pdoJaxb = null;

			pdoJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(
					pdoXmlContent);

			PatientDataType patientDataType = (PatientDataType) pdoJaxb
					.getValue();
			PatientDataResponseType patientDataResponseType = new PatientDataResponseType();
			patientDataResponseType.setPatientData(patientDataType);
			edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory objectFactory = new edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory();

			bodyType.getAny().add(
					objectFactory.createResponse(patientDataResponseType));

		} catch (JAXBUtilException e) {
			log.error("", e);
			throw new I2B2Exception("", e);
		}
		return bodyType;
	}

	private String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}
