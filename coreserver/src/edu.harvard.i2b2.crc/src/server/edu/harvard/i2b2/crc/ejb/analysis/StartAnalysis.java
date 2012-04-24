package edu.harvard.i2b2.crc.ejb.analysis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType.Condition;
import edu.harvard.i2b2.crc.exec.ExecException;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.quartz.StartJobHandler;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.I2B2ResponseMessageHelper;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class StartAnalysis implements StartAnalysisLocal {

	Connection connection = null;
	@Resource(mappedName = "ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadrunner")
	private static Queue queue;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadresponse")
	private static Queue responseQueue;
	@Resource
	private UserTransaction utx;
	// public static ApplicationContext ac;

	// log
	private static Log log = LogFactory.getLog(StartAnalysis.class);

	public MasterInstanceResultResponseType start(IDAOFactory daoFactory,
			String requestXml) throws I2B2Exception {
		StatusType statusType = new StatusType();
		String statusName = null, statusMessage = null;

		SetFinderDAOFactory sfDAOFactory = daoFactory.getSetFinderDAOFactory();

		MasterInstanceResultResponseType masterInstanceResultResponseType = null;

		String queryMasterId = null, queryInstanceId = null;
		UserType userType = null;

		QueryMaster queryMaster = new QueryMaster(sfDAOFactory);
		QueryInstance queryInstance = new QueryInstance(sfDAOFactory);

		try {
			// store the request in master table
			I2B2RequestMessageHelper msgHelper = new I2B2RequestMessageHelper(
					requestXml);
			AnalysisDefinitionType analysisDefType = msgHelper
					.getAnalysisDefinition();
			userType = msgHelper.getUserType();

			QtAnalysisPlugin analysisPlugin = queryMaster.lookupAnalysisPlugin(
					analysisDefType.getAnalysisPluginName(), analysisDefType
							.getVersion(), msgHelper.getProjectId());

			String pluginId = analysisPlugin.getPluginId();
			String domainId = userType.getGroup();
			String projectId = msgHelper.getProjectId();
			String userId = userType.getLogin();
			// call privilege bean to check for permission
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);

			authHelper.checkRoleForPluginId(pluginId);
			System.out.print("query master saved");
			utx.begin();
			String generatedSql = null;
			// save the analysis request
			queryMasterId = queryMaster.saveQuery(requestXml, generatedSql,
					analysisPlugin);

			utx.commit();
			System.out.print("query master saved1");

			// get run instance
			utx.begin();
			// save the analysis instance

			analysisDefType.getAnalysisPluginName();
			ResultOutputOptionListType resultOutputList = I2B2RequestMessageHelper
					.buildResultOptionListFromAnalysisResultList(analysisDefType
							.getCrcAnalysisResultList());

			// userType.setGroup(msgHelper.getProjectId());

			queryInstanceId = queryInstance.saveInstanceAndResultInstance(
					queryMasterId, userType, "WITHOUT_QUEUE", resultOutputList);
			utx.commit();

			// determine which queue it goes and put the jobs in that queue
			/*
			 * QueryExecutor queryExecutor = new QueryExecutor(sfDAOFactory,
			 * queryInstanceId); long timeout = msgHelper.getTimeout();
			 * queryExecutor.execute(analysisDefType, timeout);
			 */

			long timeout = msgHelper.getTimeout();
			StartJobHandler startJobHandler = new StartJobHandler(
					QueryProcessorUtil.getInstance().getQuartzScheduler());

			startJobHandler.startNonQuartzJob(domainId, projectId, userId,
					queryInstanceId, timeout);

			// Thread.sleep(timeout);
			waitForProcess(timeout, queryInstanceId);
		} catch (ExecException execEx) {
			if (execEx.getExitStatus().equals(ExecException.TIMEOUT_STATUS)) {
				statusName = StatusEnum.QUEUED.toString();
			} else {
				statusName = StatusEnum.ERROR.toString();
			}
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(execEx);
		} catch (SecurityException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (IllegalStateException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (I2B2Exception e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (JAXBUtilException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (NotSupportedException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (SystemException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (RollbackException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (HeuristicMixedException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (HeuristicRollbackException e) {
			statusName = StatusEnum.ERROR.toString();
			statusMessage = edu.harvard.i2b2.common.exception.StackTraceUtil
					.getStackTrace(e);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// just update query instance status, result instance status will be
			// updated by queryexecutor
			// queryInstance.updateInstanceStatus(queryInstanceId, statusName,
			// statusMessage);
			// if error then rollback
			// utx.setRollbackOnly();

		}

		Condition condition = new Condition();
		condition.setType(statusName);
		condition.setValue(statusMessage);
		statusType.getCondition().add(condition);
		// build masterInstanceResultResponse
		I2B2ResponseMessageHelper responseMessageHelper = new I2B2ResponseMessageHelper(
				sfDAOFactory);
		try {
			masterInstanceResultResponseType = responseMessageHelper
					.buildResponse(queryMasterId, queryInstanceId, userType
							.getLogin(), statusType);
		} catch (I2B2DAOException e) {
			throw new I2B2Exception("Error " + e.getMessage() + "] ", e);
		}
		return masterInstanceResultResponseType;
	}

	private void waitForProcess(long timeout, String instanceId) {
		MessageConsumer receiver = null;
		TextMessage message = null;
		LoadDataResponseType response = null;
		Session session = null;
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String selector = "JMSCorrelationID='" + instanceId + "'";
			receiver = session.createConsumer(responseQueue, selector);

			connection.start();

			TextMessage inMessage = (TextMessage) receiver.receive(timeout);
			if (inMessage != null) {
				System.out.println("Received text message from response queue"
						+ inMessage.getText());

			}
		} catch (JMSException jmsEx) {
			jmsEx.printStackTrace();
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}
			}

		}
	}

	public void queueProcess() {
		// write the status to table
		// if processs is small, run them by submitting to quartz right away
	}

	public void cronJobProcess() {
		// a)Cronjob for midium and long queue :
		// a.1) Look for running job if the start time>30min, kill the job and
		// change status to stop
		// a.2) Start new job
	}

	/**
	 * Creates the connection.
	 */
	@PostConstruct
	public void makeConnection() {
		try {
			connection = connectionFactory.createConnection();
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.makeConnection:" + "Exception: "
					+ t.toString());
		}
	}

	/**
	 * Closes the connection.
	 */
	@PreDestroy
	public void endConnection() throws RuntimeException {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
