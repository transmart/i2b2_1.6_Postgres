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

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * This class is the message driven bean to handle UploadMessages on the queue
 * and handing them to the Upload coordinator.
 * 
 * @author rkuttan
 * 
 * @ejb.bean name="querytool.QueryExecutorMDB"
 *           display-name="QueryTool Executor MDB"
 *           description="QueryTool Executor" destination-type="javax.jms.Queue"
 *           destination-jndi-name="jms.querytool.QueryExecutor"
 *           subscription-durability="Durable" transaction-type="Bean"
 *           jndi-name="ejb.querytool.QueryExecutorMDB"
 *           local-jndi-name="ejb.querytool.QueryExecutorMDBLocal"
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
public class QueryExecutorMDB implements MessageDrivenBean, MessageListener {

	private MessageDrivenContext sessionContext;

	private static Log log = LogFactory.getLog(QueryExecutorMDB.class);

	public static final String SMALL_QUEUE = "SMALL_QUEUE";
	public static final String MEDIUM_QUEUE = "MEDIUM_QUEUE";
	public static final String LARGE_QUEUE = "LARGE_QUEUE";

	private String callingMDBName = SMALL_QUEUE;

	/**
	 * Creates a new UploadProcessorMDB object.
	 */
	public QueryExecutorMDB() {
	}

	public QueryExecutorMDB(MessageDrivenContext sessionContext,
			String callingMDBName) {
		this.sessionContext = sessionContext;
		this.callingMDBName = callingMDBName;
	}

	/**
	 * Take the XML based message and delegate to the system coordinator to
	 * handle the actual processing
	 * 
	 * @param msg
	 *            th JMS TextMessage object containing XML data
	 */
	public void onMessage(Message msg) {
		MapMessage message = null;
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		Queue replyToQueue = null;
		UserTransaction transaction = sessionContext.getUserTransaction();
		// default timeout three minutes
		int transactionTimeout = 0;

		try {

			transactionTimeout = this.readTimeoutPropertyValue(SMALL_QUEUE);
			if (callingMDBName.equalsIgnoreCase(QueryExecutorMDB.MEDIUM_QUEUE)) {
				// four hours
				// transactionTimeout = 14400;
				transactionTimeout = this
						.readTimeoutPropertyValue(MEDIUM_QUEUE);
			} else if (callingMDBName
					.equalsIgnoreCase(QueryExecutorMDB.LARGE_QUEUE)) {
				// twelve hours
				// transactionTimeout = 43200;
				transactionTimeout = this.readTimeoutPropertyValue(LARGE_QUEUE);
			}

			transaction.setTransactionTimeout(transactionTimeout);

			transaction.begin();
			message = (MapMessage) msg;
			String sessionId = msg.getJMSCorrelationID();
			replyToQueue = (Queue) msg.getJMSReplyTo();
			log.debug("Extracting the message [" + msg.getJMSMessageID()
					+ " ] on " + callingMDBName);
			transaction.commit();
			ExecRunnable er = new ExecRunnable(transaction, transactionTimeout,
					callingMDBName, message, sessionId);
			er.execute();

		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				if (transaction.getStatus() != 4) {

					transaction.rollback();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.error("Error extracting message", ex);
		} finally {

			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			qmBeanUtil.closeAll(sender, null, conn, session);
		}
	}

	private int readTimeoutPropertyValue(String queueType) {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String timeoutStr = "";
		int timeoutVal = 0;
		try {
			if (queueType.equals(SMALL_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.small.timeoutsec");
			} else if (queueType.equals(MEDIUM_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.medium.timeoutsec");
			} else if (queueType.equals(LARGE_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.large.timeoutsec");
			}
			timeoutVal = Integer.parseInt(timeoutStr);

		} catch (I2B2Exception ex) {
			ex.printStackTrace();
		}
		return timeoutVal;

	}

	// --------------------------------
	// ejb functions
	// --------------------------------
	// Set the context.
	public void setMessageDrivenContext(MessageDrivenContext context) {
		this.sessionContext = context;
	}

	// ejb create
	public void ejbCreate() {
	}

	// ejb remove
	public void ejbRemove() throws EJBException {
	}
}
