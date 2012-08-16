/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.QueryExecutorDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * This class is the message driven bean to handle UploadMessages 
 * on the queue and handing them to the Upload coordinator.
 * 
 * @author rkuttan
 * 
 * @ejb.bean name="querytool.QueryExecutorLargeMDB" 
 * 		 display-name="QueryTool Executor Large MDB" 
 * 		description="QueryTool Executor Large"
 *           destination-type="javax.jms.Queue"
 *           destination-jndi-name="jms.querytool.QueryExecutorLarge"
 *           subscription-durability="Durable" 
 *           transaction-type="Bean"
 *           jndi-name="ejb.querytool.QueryExecutorMDBLarge"
 *           local-jndi-name="ejb.querytool.QueryExecutorMDBLargeLocal"
 *           view-type="both"
 *          
 *          
 * 
 * @ejb.resource-ref res-ref-name="jms.QueueFactory" res-type="Required"
 *                   res-auth="Container"
 * 
 *
 * @ejb.transaction type="Required"
 *   
 */
public class QueryLargeExecutorMDB implements MessageDrivenBean, MessageListener {

	

	private MessageDrivenContext sessionContext;

	private static Log log = LogFactory.getLog(QueryLargeExecutorMDB.class);

	

	

	/**
	 * Creates a new UploadProcessorMDB object.
	 */
	public QueryLargeExecutorMDB() {
	}

	/**
	 * Take the XML based message and delegate to 
	 * the system coordinator to  handle the 
	 * actual processing
	 * @param msg th JMS TextMessage 
	 * 				object containing XML data
	 */
	public void onMessage(Message msg) {
		MapMessage message = (MapMessage)msg;
		try {
			log.info("Executing from Large queue for query instance [ " + message.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM) +" ]");
		} catch (JMSException e) {
			e.printStackTrace();
		}
		//call executor mdb class with this session context
		QueryExecutorMDB queryMdb = new QueryExecutorMDB(sessionContext,QueryExecutorMDB.LARGE_QUEUE);
		queryMdb.onMessage(msg);
		
	}

	
	
	//--------------------------------
	//ejb functions
	//--------------------------------
	// Set the context.
	public void setMessageDrivenContext(MessageDrivenContext context) {
		this.sessionContext = context;
	}
	//ejb create
	public void ejbCreate() {
	}
	//ejb remove
	public void ejbRemove() throws EJBException {
	}
}
