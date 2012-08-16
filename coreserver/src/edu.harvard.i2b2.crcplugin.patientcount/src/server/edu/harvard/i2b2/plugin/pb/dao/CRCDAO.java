/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.plugin.pb.dao;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;

/**
 * DAO abstract class to provide common dao functions $Id: CRCDAO.java,v 1.8
 * 2008/03/27 23:38:50 rk903 Exp $
 * 
 * @author rkuttan
 * @see Connection
 * @see Session
 */
public abstract class CRCDAO {
	/** log * */
	protected final Log log = LogFactory.getLog(CRCDAO.class);

	protected DataSource dataSource = null;

	protected String dbSchemaName = null;

	protected DataSource getApplicationDataSource(String dataSourceName)
			throws I2B2DAOException {
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			DataSource dataSource = ServiceLocator.getInstance()
					.getAppServerDataSource(dataSourceName);
			return dataSource;
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting appliation/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getDbSchemaName() {
		return this.dbSchemaName;
	}

	public void setDbSchemaName(String dbSchemaName) {
		if (dbSchemaName != null && dbSchemaName.endsWith(".")) {
			this.dbSchemaName = dbSchemaName.trim();
		} else if (dbSchemaName != null) {
			this.dbSchemaName = dbSchemaName.trim() + ".";
		}

	}

}
