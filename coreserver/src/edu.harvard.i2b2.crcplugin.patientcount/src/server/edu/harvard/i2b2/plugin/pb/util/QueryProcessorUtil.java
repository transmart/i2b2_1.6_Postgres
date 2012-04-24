/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.plugin.pb.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;

/**
 * This is the CRC application's main utility class This utility class provides
 * support for fetching resources like datasouce, to read application
 * properties, to get ejb home,etc. $Id: QueryProcessorUtil.java,v 1.7
 * 2007/04/25 15:05:11 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryProcessorUtil {

	/** log **/
	protected final static Log log = LogFactory
			.getLog(QueryProcessorUtil.class);

	/** property file name which holds application directory name **/
	public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "patientcount_application_directory.properties";

	/** application directory property name **/
	public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.applicationdir";

	/** application property filename* */
	public static final String APPLICATION_PROPERTIES_FILENAME = "edu.harvard.i2b2.crcplugin.pb.properties";

	public static final String APPLICATION_SPRINGCONTEXT_FILENAME_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.springcontext.filename";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_DATASOURCE_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.ds.lookup.datasource";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SCHEMANAME_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.ds.lookup.schemaname";

	/** property name for metadata schema name* */
	private static final String DS_LOOKUP_SERVERTYPE_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.ds.lookup.servertype";

	private static final String ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.delegate.ontology.url";

	private static final String ONTOLOGYCELL_GETTERMINFO_URL_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.delegate.ontology.operation.getterminfo";

	private static final String ONTOLOGYCELL_GETCHILDREN_URL_PROPERTIES = "edu.harvard.i2b2.crplugin.pb.delegate.ontology.operation.getchildren";

	/** property name for pm url schema name **/
	private static final String PMCELL_WS_URL_PROPERTIES = "edu.harvard.i2b2.crcplugin.pb.ws.pm.url";

	/** class instance field* */
	private static QueryProcessorUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static Properties appProperties = null;

	private static Properties loadProperties = null;

	/** field to store app datasource* */
	private DataSource dataSource = null;

	/** single instance of spring bean factory* */
	private BeanFactory beanFactory = null;

	/**
	 * Private constructor to make the class singleton
	 */
	private QueryProcessorUtil() {
	}

	static {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			log.error(e);

		}
	}

	/**
	 * Return this class instance
	 * 
	 * @return QueryProcessorUtil
	 */
	public static QueryProcessorUtil getInstance() {
		if (thisInstance == null) {
			thisInstance = new QueryProcessorUtil();
			serviceLocator = ServiceLocator.getInstance();
		}

		// start cron job
		// startCronJob();

		return thisInstance;
	}

	/**
	 * Function to create spring bean factory
	 * 
	 * @return BeanFactory
	 */
	public BeanFactory getSpringBeanFactory() {
		if (beanFactory == null) {
			String appDir = null;
			try {
				// read application directory property file via classpath
				loadProperties = ServiceLocator
						.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
				// read directory property
				appDir = loadProperties.getProperty(APPLICATIONDIR_PROPERTIES);

			} catch (I2B2Exception e) {
				log.error(APPLICATION_DIRECTORY_PROPERTIES_FILENAME
						+ "could not be located from classpath ");
			}
			String springContextFileName = loadProperties
					.getProperty(APPLICATION_SPRINGCONTEXT_FILENAME_PROPERTIES);

			if (appDir != null) {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"file:" + appDir + "/" + springContextFileName);
				beanFactory = ctx.getBeanFactory();
			} else {
				FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
						"classpath:" + springContextFileName);
				beanFactory = ctx.getBeanFactory();
			}

		}
		return beanFactory;
	}

	public String getCRCDBLookupDataSource() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_DATASOURCE_PROPERTIES);
	}

	public String getCRCDBLookupServerType() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_SERVERTYPE_PROPERTIES);
	}

	public String getCRCDBLookupSchemaName() throws I2B2Exception {
		return getPropertyValue(DS_LOOKUP_SCHEMANAME_PROPERTIES);
	}

	public String getOntologyUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);
	}

	public String getProjectManagementCellUrl() throws I2B2Exception {
		return getPropertyValue(PMCELL_WS_URL_PROPERTIES);
	}

	/**
	 * Return app server datasource
	 * 
	 * @return datasource
	 * @throws I2B2Exception
	 * @throws SQLException
	 */
	public DataSource getSpringDataSource(String dataSourceName)
			throws I2B2Exception {
		DataSource dataSource = (DataSource) getSpringBeanFactory().getBean(
				dataSourceName);

		return dataSource;

	}

	public String getCRCPropertyValue(String propertyName) throws I2B2Exception {
		return getPropertyValue(propertyName);
	}

	// ---------------------
	// private methods here
	// ---------------------

	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {
		if (appProperties == null) {
			// read application directory property file
			loadProperties = ServiceLocator
					.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			// read application directory property
			String appDir = loadProperties
					.getProperty(APPLICATIONDIR_PROPERTIES);
			if (appDir == null) {
				throw new I2B2Exception("Could not find "
						+ APPLICATIONDIR_PROPERTIES + "from "
						+ APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			}
			String appPropertyFile = appDir + "/"
					+ APPLICATION_PROPERTIES_FILENAME;
			try {
				FileSystemResource fileSystemResource = new FileSystemResource(
						appPropertyFile);
				PropertiesFactoryBean pfb = new PropertiesFactoryBean();
				pfb.setLocation(fileSystemResource);
				pfb.afterPropertiesSet();
				appProperties = (Properties) pfb.getObject();
			} catch (IOException e) {
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
			if (appProperties == null) {
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
		}

		String propertyValue = appProperties.getProperty(propertyName);

		if ((propertyValue != null) && (propertyValue.trim().length() > 0)) {
			;
		} else {
			throw new I2B2Exception("Application property file("
					+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
	}

}
