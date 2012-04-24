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

import javax.ejb.CreateException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.ejb.PdoQueryLocal;
import edu.harvard.i2b2.crc.ejb.PdoQueryLocalHome;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * GetPDOFromInputListHandler class. $Id: GetPDOFromInputListHandler.java,v 1.10
 * 2008/07/21 19:56:56 rk903 Exp $
 * 
 * @author rkuttan
 */
public class GetPDOFromInputListHandler extends RequestHandler {
	private GetPDOFromInputListRequestType getPDOFromInputListRequestType = null;
	private String requestXml = null;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public GetPDOFromInputListHandler(String requestXml) throws I2B2Exception {
		this.requestXml = requestXml;
		try {
			getPDOFromInputListRequestType = (GetPDOFromInputListRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType.class);
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
			String domainId = getDataSourceLookup().getDomainId();
			String projectId = getDataSourceLookup().getProjectPath();
			String userId = getDataSourceLookup().getOwnerId();
			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(this
					.getDataSourceLookup().getDomainId(), getDataSourceLookup()
					.getProjectPath(), getDataSourceLookup().getOwnerId());
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);
			authHelper.checkRoleForProtectionLabel("PDO_WITHOUT_BLOB");

			PdoQueryLocalHome pdoQueryLocalHome = qpUtil.getPdoQueryLocalHome();
			PdoQueryLocal pdoQueryInfoLocal = pdoQueryLocalHome.create();
			PatientDataResponseType pdoResponseType = pdoQueryInfoLocal
					.getPlainPatientData(getDataSourceLookup(),
							getPDOFromInputListRequestType, requestXml);

			edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory objectFactory = new edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory();
			bodyType.getAny()
					.add(objectFactory.createResponse(pdoResponseType));
			// ResponseMessageType responseMessageType = new
			// ResponseMessageType();
			// responseMessageType.setMessageBody(bodyType);
			// responseString = getResponseString(responseMessageType);
		} catch (ServiceLocatorException e) {
			log.error("", e);
			throw new I2B2Exception("", e);
		} catch (CreateException e) {
			throw new I2B2Exception("", e);
		}
		return bodyType;
	}
}
