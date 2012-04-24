package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.axis2.AxisFault;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pm.RoleType;
import edu.harvard.i2b2.crc.datavo.pm.RolesType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.ejbpm.EJBPMUtil;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryExecutorDao extends CRCDAO implements IQueryExecutorDao {

	private DataSourceLookup dataSourceLookup = null,
			originalDataSourceLookup = null;
	private static Map generatorMap = null;
	private static String defaultResultType = null;
	private Map projectParamMap = new HashMap();

	static {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		generatorMap = (Map) bf.getBean("setFinderResultGeneratorMap");
		defaultResultType = (String) bf.getBean("defaultSetfinderResultType");
	}

	public QueryExecutorDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup,
			DataSourceLookup originalDataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.originalDataSourceLookup = originalDataSourceLookup;
	}

	/**
	 * This function executes the given sql and create query result instance and
	 * its collection
	 * 
	 * @param conn
	 *            db connection
	 * @param sqlString
	 * @param queryInstanceId
	 * @return query result instance id
	 * @throws I2B2DAOException
	 */
	public String executeSQL(UserTransaction transaction,
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList)
			throws CRCTimeOutException, I2B2DAOException {
		// StringTokenizer st = new StringTokenizer(sqlString,"<*>");
		String singleSql = null;
		int recordCount = 0;
		// int patientSetId = 0;
		javax.transaction.TransactionManager tm = null;
		UserTransaction ut = transaction;

		try {
			ut.setTransactionTimeout(transactionTimeout);
		} catch (SystemException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		boolean errorFlag = false, timeOutErrorFlag = false;
		Statement stmt = null;
		ResultSet resultSet = null;
		Connection manualConnection = null;
		/** Global temp table to store intermediate setfinder results* */
		String TEMP_TABLE = "#GLOBAL_TEMP_TABLE";

		/** Global temp table to store intermediate patient list * */
		String TEMP_DX_TABLE = "#DX";
		if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_TABLE = getDbSchemaName() + "#GLOBAL_TEMP_TABLE";
			TEMP_DX_TABLE = getDbSchemaName() + "#DX";

		} else if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || dsLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRES)) {
			TEMP_TABLE = getDbSchemaName() + "QUERY_GLOBAL_TEMP";
			TEMP_DX_TABLE = getDbSchemaName() + "DX";
		}
		Exception exception = null;

		InitialContext context;
		try {
			context = new InitialContext();

			// Using transaction manager instead of usertransaction
			// because the usertransaction needs the datasource to be XA.
			tm = (javax.transaction.TransactionManager) context
					.lookup("java:/TransactionManager");

			if (tm == null) {
				log.error("TransactionManager is null");
			}

			// ut = sessionContext.getUserTransaction();
			// ut.begin();
			String processTimingFlag = LogTimingUtil.getPocessTiming(originalDataSourceLookup.getProjectPath(), originalDataSourceLookup.getOwnerId(), 
					originalDataSourceLookup.getDomainId());
			if (processTimingFlag == null) { 
				processTimingFlag = ProcessTimingReportUtil.NONE;
			}
			
			projectParamMap.put(ParamUtil.PM_ENABLE_PROCESS_TIMING, processTimingFlag);
			ParamUtil projectParamUtil = new ParamUtil(); 
			String unitConversionFlag = projectParamUtil.getParam(originalDataSourceLookup.getProjectPath(), originalDataSourceLookup.getOwnerId(), 
					originalDataSourceLookup.getDomainId(), ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
			if (unitConversionFlag != null) { 
				projectParamMap.put(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION, unitConversionFlag.trim());
			}
			
			tm.begin();

			// change status of result instance to running
			IQueryResultInstanceDao psResultDao = sfDAOFactory
					.getPatientSetResultDAO();
			psResultDao.updatePatientSet(patientSetId, 2, 0);
			tm.commit();

			// check if the sql is stored, else generate and store
			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			IQueryInstanceDao queryInstaneDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstaneDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			String masterId = queryInstance.getQtQueryMaster()
					.getQueryMasterId();
			QtQueryMaster queryMaster = queryMasterDao
					.getQueryDefinition(masterId);
			String generatedSql = queryMaster.getGeneratedSql();
			if (generatedSql == null) {
				generatedSql = "";
			}
			String missingItemMessage = null, processTimingMessage = null;
			boolean missingItemFlag = false;
			if (generatedSql.trim().length() == 0) {
				// check if the sql is for patient set or encounter set
				boolean encounterSetFlag = this
						.getEncounterSetFlag(resultOutputList);

				// generate sql and store
				IQueryRequestDao requestDao = sfDAOFactory.getQueryRequestDAO();
				requestDao.setProjectParam(projectParamMap) ;
				
				
				String[] sqlResult = requestDao.buildSql(requestXml,
						encounterSetFlag);
				generatedSql = sqlResult[0];
				missingItemMessage = sqlResult[1];
				processTimingMessage = sqlResult[2];
				// if (generatedSql == null) {
				// throw new I2B2Exception(
				// "Database error unable to generate sql from query definition")
				// ;
				// } else if (generatedSql.trim().length() < 1) {
				// throw new I2B2Exception(
				// "Database error unable to generate sql from query definition")
				// ;
				// }
				tm.begin();
				queryMasterDao.updateQuerySQL(masterId, generatedSql);
				tm.commit();

				if (missingItemMessage != null
						&& missingItemMessage.trim().length() > 1) {
					missingItemFlag = true;
					tm.begin();
					queryInstance.setEndDate(new Date(System
							.currentTimeMillis()));
					// queryInstance.setMessage(missingItemMessage);
					setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 4,
							missingItemMessage);
					// update the error status to result instance
					setQueryResultInstanceStatus(sfDAOFactory, queryInstanceId,
							4, missingItemMessage);
					// queryInstaneDao.update(queryInstance, true);
					tm.commit();
				}
				
				if (processTimingMessage != null) {
					tm.begin();
					setQueryInstanceProcessTimingXml(sfDAOFactory,
							queryInstanceId,   processTimingMessage);
					tm.commit();
				}

			}
			if (missingItemFlag == false) {
				QueryExecutorHelperDao helperDao = new QueryExecutorHelperDao(
						dataSource, dataSourceLookup, originalDataSourceLookup);
				helperDao.setProcessTimingFlag(processTimingFlag);
				helperDao.executeQuery(transaction, transactionTimeout,
						dsLookup, sfDAOFactory, requestXml, sqlString,
						queryInstanceId, patientSetId, resultOutputList,
						generatedSql, tm, ut);

			}
		} catch (NamingException e) {
			exception = e;
			errorFlag = true;
		} catch (NotSupportedException e) {
			exception = e;
			errorFlag = true;
		} catch (SystemException e) {
			exception = e;
			errorFlag = true;
		} catch (SecurityException e) {
			exception = e;
			errorFlag = true;
		} catch (IllegalStateException e) {
			exception = e;
			errorFlag = true;
		} catch (RollbackException e) {
			exception = e;
			errorFlag = true;
		} catch (HeuristicMixedException e) {
			exception = e;
			errorFlag = true;
		} catch (HeuristicRollbackException e) {
			exception = e;
			errorFlag = true;
		} catch (CRCTimeOutException e) {
			throw e;
		} catch (I2B2DAOException e) {
			exception = e;
			errorFlag = true;
			throw e;
		} finally {
			// close resultset and statement
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (manualConnection != null) {
					manualConnection.close();
				}

			} catch (SQLException sqle) {
				log.error("Error closing statement/resultset ", sqle);
			}

			if (tm != null && errorFlag) {
				try {
					// tm.rollback();
					tm.rollback();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
				if (tm != null) {
					try {
						log
								.info("Trying to update error status to query instance["
										+ queryInstanceId + "]");
						if (sfDAOFactory != null) {
							// update set size and result status
							// tm.begin();
							tm.begin();
							String stacktrace = StackTraceUtil
									.getStackTrace(exception);
							if (stacktrace != null) {
								if (stacktrace.length() > 2000) {
									stacktrace = stacktrace.substring(0, 1998);
								} else {
									stacktrace = stacktrace.substring(0,
											stacktrace.length());
								}
							}
							setQueryInstanceStatus(sfDAOFactory,
									queryInstanceId, 4, stacktrace);
							// update the error status to result instance
							setQueryResultInstanceStatus(sfDAOFactory,
									queryInstanceId, 4, stacktrace);
							// tm.commit();
							tm.commit();
							log.info("Updated error status to query instance["
									+ queryInstanceId + "]");
						}
					} catch (Exception e) {
						log
								.error(
										"Error while updating error status to query instance",
										e);
						try {
							tm.rollback();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
		return patientSetId;
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
		queryInstanceDao.update(queryInstance, true);
	}

	private void setQueryInstanceProcessTimingXml(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId,  String message) throws I2B2DAOException {
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		queryInstanceDao.updateMessage(queryInstanceId, message, true); 
		
	}
	
	private void setQueryResultInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) {
		IQueryResultInstanceDao queryResultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultInstanceList = queryResultInstanceDao
				.getResultInstanceList(queryInstanceId);
		for (QtQueryResultInstance queryResultInstance : resultInstanceList) {
			queryResultInstanceDao.updatePatientSet(queryResultInstance
					.getResultInstanceId(), statusTypeId, message, 0, 0, "");
		}

	}
	
	

	public boolean getEncounterSetFlag(
			ResultOutputOptionListType resultOutputList) {
		boolean encounterFoundFlag = false;
		for (ResultOutputOptionType resultOutputOption : resultOutputList
				.getResultOutput()) {
			if (resultOutputOption.getName().equalsIgnoreCase(
					"PATIENT_ENCOUNTER_SET")) {
				encounterFoundFlag = true;
				break;
			}
		}
		return encounterFoundFlag;
	}

	/**
	 * Call PM to get user roles. The security info is taken from the request
	 * xml
	 * 
	 * @param requestXml
	 * @return
	 * @throws I2B2Exception
	 */
	public List<String> getRoleFromPM(String requestXml) throws I2B2Exception {

		I2B2RequestMessageHelper reqMsgHelper = new I2B2RequestMessageHelper(
				requestXml);
		SecurityType origSecurityType = reqMsgHelper.getSecurityType();
		String projectId = reqMsgHelper.getProjectId();

		SecurityType serviceSecurityType = PMServiceAccountUtil
				.getServiceSecurityType(origSecurityType.getDomain());
		EJBPMUtil callPMUtil = new EJBPMUtil(serviceSecurityType, projectId);
		List<String> roleList = new ArrayList<String>();
		try {
			RolesType rolesType = callPMUtil.callGetRole(origSecurityType
					.getUsername(), projectId);

			RoleType roleType = null;
			for (java.util.Iterator<RoleType> iterator = rolesType.getRole()
					.iterator(); iterator.hasNext();) {
				roleType = iterator.next();
				roleList.add(roleType.getRole());
			}

		} catch (AxisFault e) {
			throw new I2B2Exception(" Failed to get user role from PM "
					+ StackTraceUtil.getStackTrace(e));
		}
		return roleList;
		/*
		 * I2B2RequestMessageHelper reqMsgHelper = new I2B2RequestMessageHelper(
		 * requestXml); SecurityType securityType =
		 * reqMsgHelper.getSecurityType(); String projectId =
		 * reqMsgHelper.getProjectId(); // get roles from pm driver
		 * PMServiceDriver serviceDriver = new PMServiceDriver(); ProjectType
		 * projectType = null;
		 * 
		 * try { projectType = serviceDriver.checkValidUser(securityType,
		 * projectId); } catch (AxisFault e) { e.printStackTrace(); throw new
		 * I2B2Exception(" Failed to get user role from PM " +
		 * StackTraceUtil.getStackTrace(e)); } catch (JAXBUtilException e) {
		 * e.printStackTrace(); throw new
		 * I2B2Exception(" Failed to get user role from PM " +
		 * StackTraceUtil.getStackTrace(e)); } return projectType.getRole();
		 */
	}

}
