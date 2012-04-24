package edu.harvard.i2b2.crc.ejb;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.CRCTimeOutException;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.LockedoutException;
import edu.harvard.i2b2.crc.dao.setfinder.QueryExecutorDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class ExecRunnable {
	private static Log log = LogFactory.getLog(ExecRunnable.class);

	public static final String SMALL_QUEUE = "SMALL_QUEUE";
	public static final String MEDIUM_QUEUE = "MEDIUM_QUEUE";
	public static final String LARGE_QUEUE = "LARGE_QUEUE";

	private String callingMDBName = SMALL_QUEUE, sessionId = "";
	UserTransaction transaction = null;
	// default timeout three minutes
	int transactionTimeout = 0;
	MapMessage message = null;
	boolean finishedFlag = false;

	public ExecRunnable(UserTransaction transaction, int transactionTimeout,
			String callingMDBName, MapMessage message, String sessionId) {
		this.transaction = transaction;
		this.transactionTimeout = transactionTimeout;
		this.callingMDBName = callingMDBName;
		this.message = message;
		this.sessionId = sessionId;
	}

	public void execute() {

		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		Queue replyToQueue = null;
		try {
			if (message != null) {
				replyToQueue = (Queue) message.getJMSReplyTo();
				String sqlString = message
						.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM);
				String queryInstanceId = message
						.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM);
				String patientSetId = message
						.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM);
				String xmlRequest = message
						.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM);

				String dsLookupDomainId = message
						.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID);
				String dsLookupProjectId = message
						.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID);
				String dsLookupOwnerId = message
						.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID);
				System.out.println("domain id" + dsLookupDomainId + " "
						+ dsLookupProjectId + " " + dsLookupOwnerId
						+ " *********************");

				DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
						dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);

				/*
				 * DataSourceLookupHelper dataSourceHelper = new
				 * DataSourceLookupHelper(); DataSourceLookup dsLookup =
				 * dataSourceHelper.matchDataSource( dsLookupDomainId,
				 * dsLookupProjectId, dsLookupOwnerId);
				 */

				IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();

				SetFinderDAOFactory sfDAOFactory = daoFactory
						.getSetFinderDAOFactory();
				DataSourceLookup dsLookup = sfDAOFactory.getDataSourceLookup();
				System.out.println("ORIG domain id"
						+ sfDAOFactory.getOriginalDataSourceLookup()
								.getDomainId()
						+ " ORIG "
						+ sfDAOFactory.getOriginalDataSourceLookup()
								.getProjectPath()
						+ " ORIG "
						+ sfDAOFactory.getOriginalDataSourceLookup()
								.getOwnerId());

				try {
					// check if the status is cancelled
					IQueryInstanceDao queryInstanceDao = sfDAOFactory
							.getQueryInstanceDAO();
					QtQueryInstance queryInstance = queryInstanceDao
							.getQueryInstanceByInstanceId(queryInstanceId);
					int queryStatusId = queryInstance.getQtQueryStatusType()
							.getStatusTypeId();
					if (queryStatusId == 9) {
						log
								.info("Ignoring this query, query status was 'Cancelled'");
					} else {
						// set the query instance batch mode to queue name
						queryInstance.setBatchMode(this.callingMDBName);
						//queryInstance.setEndDate(new Date(System
						//		.currentTimeMillis()));
						queryInstanceDao.update(queryInstance, false);

						// process the query request
						patientSetId = processQueryRequest(transaction,
								transactionTimeout, dsLookup, sfDAOFactory,
								xmlRequest, sqlString, sessionId,
								queryInstanceId, patientSetId);
						log
								.debug("QueryExecutorMDB completed processing query instance ["
										+ queryInstanceId + "]");
						// finally send reply to queue
						sendReply(sessionId, patientSetId, "", replyToQueue);
					}

				} catch (CRCTimeOutException daoEx) {
					// catch this error and ignore. send general reply message.
					log.error(daoEx.getMessage(), daoEx);
					if (callingMDBName.equalsIgnoreCase(LARGE_QUEUE)) {
						transaction.begin();
						// set status to error
						setQueryInstanceStatus(sfDAOFactory, queryInstanceId,
								10,
								"Could not complete the query in the large queue with transaction timeout "
										+ transactionTimeout);
						transaction.commit();

					} else {

						// send message to next queue and if the there is no
						// next queue then update query instance to error
						tryNextQueue(sfDAOFactory, sessionId, message,
								queryInstanceId);

					}
				} catch (I2B2DAOException daoEx) {
					if (daoEx instanceof LockedoutException) {
						log.debug("Lockedout happend"
								+ daoEx.getMessage());
						// message.
						log.error(daoEx.getMessage(), daoEx);
						// finally send reply to queue
						sendReply(sessionId, patientSetId, daoEx.getMessage(),
								replyToQueue);
					} else {
						// catch this error and ignore. send general reply
						// message.
						log.error(daoEx.getMessage(), daoEx);
						// finally send reply to queue
						sendReply(sessionId, patientSetId, "", replyToQueue);
					}
				}
			}
			// setFinishedFlag(true);
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

	private void sendReply(String sessionId, String patientSetId,
			String message, Queue replyToQueue) throws JMSException,
			ServiceLocatorException {
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			ServiceLocator serviceLocator = ServiceLocator.getInstance();
			conn = serviceLocator.getQueueConnectionFactory(
					QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
					.createQueueConnection();
			session = conn.createQueueSession(false,
					javax.jms.Session.AUTO_ACKNOWLEDGE);
			MapMessage mapMessage = session.createMapMessage();
			// mapMessage.setString("para1", responseXML);
			log.debug("message session id " + sessionId);
			mapMessage.setJMSCorrelationID(sessionId);
			mapMessage.setString(
					QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM,
					patientSetId);
			mapMessage.setString(QueryManagerBeanUtil.QUERY_STATUS_PARAM,
					message);
			sender = session.createSender(replyToQueue);
			sender.send(mapMessage);
		} catch (JMSException jmse) {
			throw jmse;
		} finally {
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			qmBeanUtil.closeAll(sender, null, conn, session);
		}

	}

	private void tryNextQueue(SetFinderDAOFactory sfDAOFactory,
			String sessionId, MapMessage msg, String queryInstanceId)
			throws JMSException, ServiceLocatorException {
		String jmsQueueName = null;

		// check which queue is this
		if (callingMDBName.equalsIgnoreCase(SMALL_QUEUE)) {
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.MEDIUM_QUEUE_NAME;
			// this.setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 7,
			// "Queued in MEDIUM queue");
		} else if (callingMDBName.equalsIgnoreCase(MEDIUM_QUEUE)) {
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.LARGE_QUEUE_NAME;
			// this.setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 8,
			// "Queued in LARGE queue");
		}

		if (jmsQueueName != null) {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			ServiceLocator serviceLocator = ServiceLocator.getInstance();
			QueueConnection conn = serviceLocator.getQueueConnectionFactory(
					QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
					.createQueueConnection();
			Queue responseQueue = serviceLocator
					.getQueue(QueryManagerBeanUtil.RESPONSE_QUEUE_NAME);
			Queue sendQueue = serviceLocator.getQueue(jmsQueueName);

			QueueSession session = conn.createQueueSession(false,
					javax.jms.Session.AUTO_ACKNOWLEDGE);
			String id = sessionId;
			String selector = "JMSCorrelationID='" + id + "'";
			QueueSender sender = session.createSender(sendQueue);
			MapMessage mapMsg = session.createMapMessage();
			mapMsg.setJMSCorrelationID(id);
			mapMsg.setJMSReplyTo(responseQueue);

			mapMsg.setString(QueryManagerBeanUtil.XML_REQUEST_PARAM, msg
					.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM));
			mapMsg
					.setString(
							QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM,
							msg
									.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM, msg
					.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM));
			mapMsg
					.setString(
							QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM,
							msg
									.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID, msg
					.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID));

			sender.send(mapMsg);
		}
	}

	private String processQueryRequest(UserTransaction transaction,
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String xmlRequest,
			String sqlString, String sessionId, String queryInstanceId,
			String patientSetId) throws I2B2DAOException, I2B2Exception {

		// QueryRequestDao queryRequestDao = new QueryRequestDao();
		// returnedPatientSetId =
		// queryRequestDao.getPatientCount(queryRequestXml,
		// queryInstanceId,patientSetId);
		QueryDefinitionRequestType qdRequestType = getQueryDefinitionRequestType(xmlRequest);
		ResultOutputOptionListType resultOutputList = qdRequestType
				.getResultOutputList();
		DataSource dataSource = ServiceLocator.getInstance()
				.getAppServerDataSource(dsLookup.getDataSource());

		QueryExecutorDao queryExDao = new QueryExecutorDao(dataSource,
				dsLookup, sfDAOFactory.getOriginalDataSourceLookup());

		queryExDao.executeSQL(transaction, transactionTimeout, dsLookup,
				sfDAOFactory, xmlRequest, sqlString, queryInstanceId,
				patientSetId, resultOutputList);
		return patientSetId;
	}

	private QueryDefinitionRequestType getQueryDefinitionRequestType(
			String xmlRequest) throws I2B2Exception {
		String queryName = null;
		QueryDefinitionType queryDef = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = null;
		QueryDefinitionRequestType queryDefReqType = null;
		try {
			jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

			if (jaxbElement == null) {
				throw new I2B2Exception(
						"null value in after unmarshalling request string ");
			}

			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();
			requestMessageType.getMessageHeader().getSecurity();
			requestMessageType.getMessageHeader().getProjectId();

			BodyType bodyType = requestMessageType.getMessageBody();
			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
					.getObjectByClass(bodyType.getAny(),
							QueryDefinitionRequestType.class);
		} catch (JAXBUtilException e) {
			log.error(e.getMessage(), e);
			throw new I2B2Exception(e.getMessage(), e);
		}
		return queryDefReqType;

	}

	private void setQueryInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) throws I2B2DAOException {

		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(queryInstanceId);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(statusTypeId);
		queryInstance.setQtQueryStatusType(queryStatusType);
		queryInstance.setEndDate(new Date(System.currentTimeMillis()));
		queryInstance.setMessage(message);
		queryInstance.setBatchMode(callingMDBName);
		queryInstanceDao.update(queryInstance, true);
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

}
