/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import java.util.List;

import javax.ejb.CreateException;
import javax.management.MalformedObjectNameException;

import org.jboss.cache.Cache;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryManagerLocal;
import edu.harvard.i2b2.crc.ejb.QueryManagerLocalHome;
import edu.harvard.i2b2.crc.ejb.role.PriviledgeLocal;
import edu.harvard.i2b2.crc.util.CacheUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * RunQueryInstanceFromQueryDefinitionHandler class implements execute method
 * $Id: RunQueryInstanceFromQueryDefinitionHandler.java,v 1.6 2008/03/19
 * 22:36:37 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RunQueryInstanceFromQueryDefinitionHandler extends RequestHandler {
	QueryDefinitionRequestType queryDefRequestType = null;
	String requestXml = null;
	boolean lockedoutFlag = false,errorFlag = false;

	/**
	 * Constuctor which accepts i2b2 request message xml
	 * 
	 * @param requestXml
	 * @throws I2B2Exception
	 */
	public RunQueryInstanceFromQueryDefinitionHandler(String requestXml)
			throws I2B2Exception {
		try {
			queryDefRequestType = (QueryDefinitionRequestType) getRequestType(
					requestXml,
					edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
			this.requestXml = requestXml;
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	/**
	 * Perform operation for the given request using business class(ejb) and
	 * return response
	 * 
	 * @throws I2B2Exception
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandler#execute()
	 */
	public BodyType execute() throws I2B2Exception {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String response = null;
		BodyType bodyType = new BodyType();
		MasterInstanceResultResponseType masterInstanceResponse = null;
		try {
			PriviledgeLocal privilegeLocal = qpUtil.getPriviledgeLocal();

			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(this
					.getDataSourceLookup().getDomainId(), getDataSourceLookup()
					.getProjectPath(), getDataSourceLookup().getOwnerId());

			Cache cache = null;
			try {
				cache = CacheUtil.getCache();
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// domainId + "/" + projectId + "/" + userId;
			String rolePath = getDataSourceLookup().getDomainId() + "/"
					+ getDataSourceLookup().getProjectPath() + "/"
					+ getDataSourceLookup().getOwnerId();

			List<String> roles = (List<String>) cache.getRoot().get(rolePath);
			log.debug("Roles from get " + rolePath);
			if (roles != null) {
				log.debug("Roles from size " + roles.size());
			} else {
				log.debug("Roles from get is null ");
			}

			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			privilegeLocal.checkPriviledge(daoFactory,
					"SETFINDER_QRY_WITH_DATAOBFSC", roles);

			QueryManagerLocalHome queryManagerLocalHome = qpUtil
					.getQueryManagerLocalHome();

			QueryManagerLocal queryManagerLocal = queryManagerLocalHome
					.create();

			// response = queryManagerLocal.processQuery(requestXml);
			masterInstanceResponse = queryManagerLocal.processQuery(this
					.getDataSourceLookup(), requestXml);
			if (masterInstanceResponse.getStatus() != null) {
				StatusType status = masterInstanceResponse.getStatus();
				if (status.getCondition().get(0) != null) {
					if (status.getCondition().get(0).getValue() != null) {
						if (status.getCondition().get(0).getValue().indexOf(
								"LOCKEDOUT") > -1) {
							lockedoutFlag = true;
						}
					}
				}
			}
			QueryInstanceType queryInstance = masterInstanceResponse.getQueryInstance();
			QueryStatusTypeType statusType = queryInstance.getQueryStatusType();
			if (statusType.getStatusTypeId() != null && statusType.getStatusTypeId().trim().equals("4")) { 
				StatusType status =  new StatusType();
				StatusType.Condition condition = new StatusType.Condition();
				condition.setType("ERROR");
				condition.setValue("ERROR");
				status.getCondition().add(condition);
				masterInstanceResponse.setStatus(status);
				errorFlag = true;
			}
			// masterInstanceResponse.setStatus(this.buildCRCStausType(
			// RequestHandlerDelegate.DONE_TYPE, "DONE"));
			
			// response = this.buildResponseMessage(requestXml, bodyType);
		} catch (I2B2Exception e) {
			masterInstanceResponse = new MasterInstanceResultResponseType();
			masterInstanceResponse.setStatus(this.buildCRCStausType(
					RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
		} catch (ServiceLocatorException e) {
			log.error(e);
			throw new I2B2Exception("Servicelocator exception", e);
		} catch (CreateException e) {
			log.error(e);
			throw new I2B2Exception("Ejb create exception", e);
		} finally {
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory psmObjFactory = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			bodyType.getAny().add(
					psmObjFactory.createResponse(masterInstanceResponse));
		}

		return bodyType;
	}

	public boolean getLockedoutFlag() {
		return lockedoutFlag;
	}
	
	public boolean getErrorFlag() { 
		return errorFlag;
	}
}
