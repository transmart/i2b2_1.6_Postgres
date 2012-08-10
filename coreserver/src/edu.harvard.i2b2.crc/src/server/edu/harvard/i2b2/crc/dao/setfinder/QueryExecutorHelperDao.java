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

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.axis2.AxisFault;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pm.RoleType;
import edu.harvard.i2b2.crc.datavo.pm.RolesType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.delegate.ejbpm.EJBPMUtil;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * This class executes the setfinder query's panel sql. The temp table stores
 * the patient/visits for the query and the temp table will be passed to rest of
 * the result type generator class.
 * 
 */
public class QueryExecutorHelperDao extends CRCDAO {

	private DataSourceLookup dataSourceLookup = null,
			originalDataSourceLookup = null;
	private static Map generatorMap = null;
	private static String defaultResultType = null;
	private String processTimingFlag = ProcessTimingReportUtil.NONE;
	private ProcessTimingReportUtil ptrUtil = null;
	private boolean queryWithoutTempTableFlag = false;
	
	static {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		generatorMap = (Map) bf.getBean("setFinderResultGeneratorMap");
		defaultResultType = (String) bf.getBean("defaultSetfinderResultType");
	}

	public QueryExecutorHelperDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup,
			DataSourceLookup originalDataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.originalDataSourceLookup = originalDataSourceLookup;
		
	}
	
	public void setProcessTimingFlag(String processTimingFlag) { 
		this.processTimingFlag = processTimingFlag;
		
		try {
			ptrUtil = new ProcessTimingReportUtil(dataSourceLookup);
		} catch (I2B2DAOException e) {
			log.error("Error trying to construct ProcessTimingReportUtil" + e.getMessage());
		}
	}
	
	public void setQueryWithoutTempTableFlag(boolean queryWithoutTempTableFlag) { 
		this.queryWithoutTempTableFlag = queryWithoutTempTableFlag;
	}
	

	/**
	 * Execute the Panel's sql to write the patient/visit list into the temp
	 * table.
	 * 
	 * And then pass this temp table data to rest of the result type generator.
	 * 
	 * @param transaction
	 * @param transactionTimeout
	 * @param dsLookup
	 * @param sfDAOFactory
	 * @param requestXml
	 * @param sqlString
	 * @param queryInstanceId
	 * @param patientSetId
	 * @param resultOutputList
	 * @param generatedSql
	 * @param tm
	 * @param ut
	 * @throws CRCTimeOutException
	 * @throws I2B2DAOException
	 */
	public void executeQuery(UserTransaction transaction,
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, String generatedSql,
			javax.transaction.TransactionManager tm, UserTransaction ut)
			throws CRCTimeOutException, I2B2DAOException {
		boolean errorFlag = false, timeOutErrorFlag = false;
		Statement stmt = null;
		ResultSet resultSet = null;
		Connection manualConnection = null;
		String singleSql = null;
		int recordCount = 0, obfuscatedRecordCount = 0;

		/** Global temp table to store intermediate setfinder results* */
		String TEMP_TABLE = "#GLOBAL_TEMP_TABLE";

		/** Global temp table to store intermediate patient list * */
		String TEMP_DX_TABLE = "#DX";
		String TEMP_MASTER_TABLE = "#MASTER_GLOBAL_TEMP_TABLE";
		
		if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_TABLE = getDbSchemaName() + "#GLOBAL_TEMP_TABLE";
			TEMP_DX_TABLE = getDbSchemaName() + "#DX";
			TEMP_MASTER_TABLE = getDbSchemaName() + "#MASTER_GLOBAL_TEMP_TABLE";
		} else if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			TEMP_TABLE = getDbSchemaName() + "QUERY_GLOBAL_TEMP";
			TEMP_DX_TABLE = getDbSchemaName() + "DX";
			TEMP_MASTER_TABLE = getDbSchemaName() + "MASTER_QUERY_GLOBAL_TEMP";
		}
		Exception exception = null;

		try {

			// manualConnection =
			// QueryProcessorUtil.getInstance().getManualConnection();
			manualConnection = ServiceLocator.getInstance()
					.getAppServerDataSource(dsLookup.getDataSource())
					.getConnection();
			// manualConnection =
			// QueryProcessorUtil.getInstance().getSpringDataSource(dsLookup.
			// getDataSource()).getConnection();
			// manualConnection =
			// QueryProcessorUtil.getInstance().getConnection();
			stmt = manualConnection.createStatement();
			int count = 0;

			if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				String checkDeleteGlobalTempTable = "drop table " + TEMP_TABLE;
				String checkDeleteCountTable = "drop table " + TEMP_DX_TABLE;
				String checkDeleteMasterTable = "drop table " + TEMP_MASTER_TABLE;
				Statement clearTempStmt = manualConnection.createStatement();
				try {
					clearTempStmt.executeUpdate(checkDeleteGlobalTempTable);
				} catch (SQLException dEx) {
					;
				}
				try {
					clearTempStmt.executeUpdate(checkDeleteCountTable);
				} catch (SQLException dEx) {
					;
				}
				try {
					clearTempStmt.executeUpdate(checkDeleteMasterTable);
				} catch (SQLException dEx) {
					;
				}
				clearTempStmt.close();

				String createSql = "CREATE  TABLE " + TEMP_TABLE + " ( "
						+ " ENCOUNTER_NUM int, " + " PATIENT_NUM int, INSTANCE_NUM int, CONCEPT_CD varchar(50), START_DATE DATETIME, PROVIDER_ID varchar(50), "
						+ " PANEL_COUNT int, " + " fact_count int, "
						+ " fact_panels int " + " )";

				stmt.executeUpdate(createSql);
				createSql = " CREATE  TABLE " + TEMP_DX_TABLE + "  ( "
						+ " ENCOUNTER_NUM int, " + " PATIENT_NUM int, INSTANCE_NUM int, CONCEPT_CD varchar(50), START_DATE DATETIME, PROVIDER_ID varchar(50) ) ";
				stmt.executeUpdate(createSql);
				createSql = " CREATE  TABLE " + TEMP_MASTER_TABLE + "  ( "
				+ " ENCOUNTER_NUM int,  PATIENT_NUM int , INSTANCE_NUM int, CONCEPT_CD varchar(50), START_DATE DATETIME, PROVIDER_ID varchar(50), MASTER_ID varchar(50), LEVEL_NO int ) ";
				stmt.executeUpdate(createSql);
		
				if (dsLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
					String indexSql = "create index tempIndex on "
							+ this.getDbSchemaName()
							+ "#global_temp_table (patient_num,encounter_num,panel_count)";
					log.debug("Executing sql [ " + indexSql + " ]");
					stmt.executeUpdate(indexSql);

				}

			} else if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				String clearGlobalTempTable = "delete from " + TEMP_TABLE;
				String clearCountTable = "delete from " + TEMP_DX_TABLE;
				String clearMasterTable = "delete from " + TEMP_MASTER_TABLE;
				
				Statement clearTempStmt = manualConnection.createStatement();
				try {
					clearTempStmt.executeUpdate(clearGlobalTempTable);
					clearTempStmt.executeUpdate(clearCountTable);
					clearTempStmt.executeUpdate(clearMasterTable);
				} catch (SQLException dEx) {
					;
				} finally {
					clearTempStmt.close();
				}

			}
			// set transaction timeout
			stmt.setQueryTimeout(transactionTimeout);
			// start seperate thread to cancel the running sql if the
			// stmt.setQueryTimeout did not work
			CancelStatementRunner csr = new CancelStatementRunner(stmt,
					transactionTimeout);
			Thread csrThread = new Thread(csr);
			csrThread.start();

			// ut.commit();
			String[] sqls = null; 
			if (this.queryWithoutTempTableFlag == false) { 
				sqls = generatedSql.split("<\\*>");
			} else { 
				sqls = new String[] {generatedSql};
			}
			
			// UserTransaction could not be used here because it needs a
			// XA datasource.
			try {
				log.debug("Transaction timeout in sec " + transactionTimeout);
				ut.setTransactionTimeout(transactionTimeout);
			} catch (SystemException e2) {
				e2.printStackTrace();
			}

			long startTime = System.currentTimeMillis();
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			LogTimingUtil outerLogTimingUtil = new LogTimingUtil();
			outerLogTimingUtil.setStartTime();
			
			// execute generated sql
			while (count < sqls.length) {
				singleSql = sqls[count++];
				if (singleSql != null && singleSql.trim().length() > 10) {
					log.debug("Executing setfinder query sql [" + singleSql + "]"
							+ " for query instance= " + queryInstanceId);
					logTimingUtil.setStartTime();
					if (this.queryWithoutTempTableFlag == false) { 
						int rows = stmt.executeUpdate(singleSql);
						log.debug("Rows affected [" + rows + "] for query instance");
						// if the database is sqlserver, then updating the temp
						// table statistics speed up the query
						if (this.dataSourceLookup.getServerType().equalsIgnoreCase(
								DAOFactoryHelper.SQLSERVER)) {
							log.debug("UPDATE STATISTICS " + this.getDbSchemaName()
									+ "#global_temp_table ");
							stmt.executeUpdate("UPDATE STATISTICS "
									+ this.getDbSchemaName()
									+ "#global_temp_table ");
						}
					} else { 
						resultSet = stmt.executeQuery(singleSql);
						
					}
					logTimingUtil.setEndTime();
					
					
					
					//log the time to the query instance
					if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) {
						ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil,"EXECUTE SQL - ITEM ","sql="+singleSql)); 
					}
				}
			}
			
			outerLogTimingUtil.setEndTime();
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			log.debug("Time to run the SETFINDER query query instance id ["
					+ queryInstanceId + "] is [" + totalTime + " ]");
			
			//log the process timing to the query instance 
			if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.INFO)) {
				ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(outerLogTimingUtil,"EXECUTE SQL",null));
			}
			
			// set the cancel thread the
			csr.setSqlFinishedFlag();
			csrThread.interrupt();

			String fetchSql = " select count(distinct patient_num) as patient_num_count from "
					+ TEMP_DX_TABLE;
			Statement countStmt = manualConnection.createStatement();
			if (this.queryWithoutTempTableFlag == false) { 
				resultSet = countStmt.executeQuery(fetchSql);
			}
			
			int i = 0;
			//read count sigma from property file
			double countSigma = GaussianBoxMuller.getCountSigma();
			double obfuscatedMinimumValue = GaussianBoxMuller.getObfuscatedMinimumVal();
			while (resultSet.next() && (i++ < 10)) {
				recordCount = resultSet.getInt("patient_num_count");
				GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
				obfuscatedRecordCount = (int) gaussianBoxMuller
						.getNormalizedValueForCount(recordCount,countSigma,obfuscatedMinimumValue);
				
				log.debug("Calculated Patient set size :[" + recordCount
						+ "] for query instance= " + queryInstanceId);
			}
			countStmt.close();
			resultSet.close();

			// tm.begin();
			// call the result generator with the db connection/temp table
			callResultGenerator(resultOutputList, manualConnection,
					sfDAOFactory, requestXml, patientSetId, queryInstanceId,
					TEMP_DX_TABLE, recordCount, obfuscatedRecordCount, transactionTimeout, tm);

			// delete temp table
			String deleteGlobalTempTable = "";
			String deleteCountTable = "", deleteMasterTable = "";
			
			if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteGlobalTempTable = "drop table " + TEMP_TABLE;
				deleteCountTable = "drop table " + TEMP_DX_TABLE;
				deleteMasterTable = "drop table " + TEMP_MASTER_TABLE;
			} else if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				deleteGlobalTempTable = "delete from " + TEMP_TABLE;
				deleteCountTable = "delete from " + TEMP_DX_TABLE;
				deleteMasterTable = "delete from " + TEMP_MASTER_TABLE;
				
			}

			Statement deleteStmt = manualConnection.createStatement();
			Statement deleteStmt1 = manualConnection.createStatement();
			
			log.debug("Executing Sql [" + deleteGlobalTempTable + "]");
			deleteStmt.executeUpdate(deleteGlobalTempTable);
			log.debug("Executing Sql [" + deleteCountTable + "]");
			deleteStmt1.executeUpdate(deleteCountTable);
			log.debug("Executing Sql [" + deleteMasterTable + "]");
			deleteStmt1.executeUpdate(deleteMasterTable);
			deleteStmt.close();
			deleteStmt1.close();

			// update query instance restult status
			tm.begin();
			setQueryInstanceStatus(sfDAOFactory, queryInstanceId, 6, null);
			tm.commit();
			log.debug("Query executor completed processing query instance[ "
					+ queryInstanceId + " ]");

		} catch (CRCTimeOutException timeoutEx) {
			timeOutErrorFlag = true;
			throw timeoutEx;
		} catch (com.microsoft.sqlserver.jdbc.SQLServerException sqlServerEx) {

			if (sqlServerEx.getMessage().indexOf("The query was canceled.") > -1) {
				timeOutErrorFlag = true;
				throw new CRCTimeOutException(sqlServerEx.getMessage(),
						sqlServerEx);
			}
			if (sqlServerEx.getMessage().indexOf("timed out") > -1) {
				timeOutErrorFlag = true;
				throw new CRCTimeOutException(sqlServerEx.getMessage(),
						sqlServerEx);
			} else {
				errorFlag = true;
				exception = sqlServerEx;
				log.error("Sqlserver error while executing sql", sqlServerEx);
				throw new I2B2DAOException(
						"Sqlserver error while executing sql", sqlServerEx);
			}

		} catch (SQLException sqlEx) {
			if (sqlEx.toString().indexOf("ORA-01013") > -1) {
				timeOutErrorFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			errorFlag = true;
			exception = sqlEx;
			log.error("Error while executing sql", sqlEx);
			throw new I2B2DAOException("Error while executing sql", sqlEx);

		} catch (LockedoutException crcDaoEx) {
			// I2B2DAOException
			errorFlag = true;
			exception = crcDaoEx;
			throw crcDaoEx;
		} catch (I2B2Exception i2b2Ex) {
			errorFlag = true;
			exception = i2b2Ex;
			log.error("Error executing " + i2b2Ex.getMessage(), i2b2Ex);
			throw new I2B2DAOException("Error executing  "
					+ i2b2Ex.getMessage(), i2b2Ex);

		} catch (IllegalStateException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("IllegalState exception", e);
		} catch (SystemException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("System exception", e);
		} catch (NotSupportedException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("System exception", e);
		} catch (SecurityException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("SecurityException", e);
		} catch (RollbackException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("RollbackException", e);
		} catch (HeuristicMixedException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("HeuristicMixedException", e);
		} catch (HeuristicRollbackException e) {
			errorFlag = true;
			exception = e;
			e.printStackTrace();
			throw new I2B2DAOException("HeuristicRollbackException", e);
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

			if (errorFlag) {
				handleException(tm, errorFlag, queryInstanceId, sfDAOFactory,
						exception);
			}

		}

	}

	private void handleException(javax.transaction.TransactionManager tm,
			boolean errorFlag, String queryInstanceId,
			SetFinderDAOFactory sfDAOFactory, Exception exception) {
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
					log.info("Trying to update error status to query instance["
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
								stacktrace = stacktrace.substring(0, stacktrace
										.length());
							}
						}
						setQueryInstanceStatus(sfDAOFactory, queryInstanceId,
								4, stacktrace);
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

	private void setQueryResultInstanceStatus(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, int statusTypeId, String message) {
		IQueryResultInstanceDao queryResultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultInstanceList = queryResultInstanceDao
				.getResultInstanceList(queryInstanceId);
		for (QtQueryResultInstance queryResultInstance : resultInstanceList) {
			queryResultInstanceDao.updatePatientSet(queryResultInstance
					.getResultInstanceId(), statusTypeId, message, -1, -1, "");
		}

	}

	@SuppressWarnings("unchecked")
	private void callResultGenerator(
			ResultOutputOptionListType resultOutputList,
			Connection manualConnection, SetFinderDAOFactory sfDAOFactory,
			String requestXml, String patientSetId, String queryInstanceId,
			String TEMP_DX_TABLE, int recordCount, int obfuscatedRecordCount, int transactionTimeout, TransactionManager tm)
			throws CRCTimeOutException, LockedoutException, I2B2DAOException {

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

		BeanFactory bf = qpUtil.getSpringBeanFactory();
		// Map ontologyKeyMap = (Map)
		// bf.getBean("setFinderResultOntologyKeyMap");
		Map ontologyKeyMap = (Map) new HashMap();

		CallOntologyUtil callOntologyUtil = null;
		try {

			String ontologyUrl = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.delegate.ontology.url");
			String getChildrenOperationName = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.delegate.ontology.operation.getchildren");
			String ontologyGetChildrenUrl = ontologyUrl
					+ getChildrenOperationName;
			log.debug("Ontology getChildren url from property file ["
					+ ontologyGetChildrenUrl + "]");
			callOntologyUtil = new CallOntologyUtil(ontologyGetChildrenUrl,
					requestXml);
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2DAOException(e.getMessage());
		} catch (I2B2Exception e) {
			e.printStackTrace();
			throw new I2B2DAOException(e.getMessage());
		}

		// get roles either from cache or by calling PM

		List<String> roles = new ArrayList<String>();

		try {
			roles = getRoleFromPM(requestXml);
		} catch (I2B2Exception e) {
			throw new I2B2DAOException(e.getMessage());
		}
		int lockoutQueryCount = -1, lockoutQueryDay = -1, lockoutQueryZeroCount = -1;
		boolean dataObfuscFlag = false;
		try {
			DataSourceLookup dataSourceLookup = sfDAOFactory
					.getDataSourceLookup();

			String domainId = dataSourceLookup.getDomainId();
			String projectId = dataSourceLookup.getProjectPath();
			String userId = dataSourceLookup.getOwnerId();

			DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectId,
					userId);
			IDAOFactory daoFactory = helper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);
			boolean noDataAggFlag = false, noDataObfuscFlag = false;
			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITHOUT_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataAggFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}
			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITH_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataObfuscFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}

			if (noDataAggFlag && !noDataObfuscFlag) {
				dataObfuscFlag = true;
			}
			String lockoutQueryCountStr = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.lockout.setfinderquery.count");
			if (lockoutQueryCountStr != null) {
				lockoutQueryCount = Integer.parseInt(lockoutQueryCountStr);
			}
			String lockoutQueryDayStr = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.lockout.setfinderquery.day");
			if (lockoutQueryDayStr != null) {
				lockoutQueryDay = Integer.parseInt(lockoutQueryDayStr);
			}
			String lockoutQueryZeroCountStr = null;
			
			lockoutQueryZeroCountStr = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.lockout.setfinderquery.zero.count");
			if (lockoutQueryZeroCountStr != null) {
				lockoutQueryZeroCount = Integer.parseInt(lockoutQueryZeroCountStr);
			}
			
		} catch (I2B2Exception e) {
			throw new I2B2DAOException(e.getMessage());
		}
		Map param = new HashMap();
		SetFinderConnection sfConn = new SetFinderConnection(manualConnection);
		param.put("SetFinderConnection", sfConn);
		param.put("SetFinderDAOFactory", sfDAOFactory);
		param.put("PatientSetId", patientSetId);
		param.put("QueryInstanceId", queryInstanceId);
		param.put("TEMP_DX_TABLE", TEMP_DX_TABLE);
		param.put("setFinderResultOntologyKeyMap", ontologyKeyMap);
		param.put("ServerType", this.dataSourceLookup.getServerType());
		param.put("CallOntologyUtil", callOntologyUtil);
		param.put("OriginalDataSourceLookup", this.originalDataSourceLookup);
		param.put("Roles", roles);
		param.put("TransactionTimeout", transactionTimeout);
		param.put("TransactionManager", tm);
		param.put("ProcessTimingFlag", processTimingFlag);
		param.put("ObfuscatedRecordCount", obfuscatedRecordCount);
		param.put("RecordCount", recordCount);
		param.put("ObfuscatedRoleFlag", dataObfuscFlag);

		if (resultOutputList != null) {
			if (resultOutputList.getResultOutput() != null
					&& resultOutputList.getResultOutput().size() > 0) {
				List<ResultOutputOptionType> resultOptionList = resultOutputList
						.getResultOutput();
				for (ResultOutputOptionType resultOutputOption : resultOptionList) {
					String resultName = resultOutputOption.getName()
							.toUpperCase();
					String resultInstanceId = getQueryResultInstanceId(
							sfDAOFactory, queryInstanceId, resultName);
					param.put("ResultInstanceId", resultInstanceId);
					param.put("ResultOptionName", resultName);
					// ::TODO check if the result state is completed, before
					// running the result
					runGenerator(resultName, param);
					// check if the user need to be locked
					// if the lockoutQueryCount = -1 skip lockout check
					if (lockoutQueryCount>-1) {
						// resultInstanceId, userId
						if (dataObfuscFlag) {
							String userLockedDate = null;
							try {
								userLockedDate = processUserLock(sfDAOFactory,
										requestXml, resultInstanceId,
										lockoutQueryCount, lockoutQueryDay,lockoutQueryZeroCount);
							} catch (I2B2Exception e) {
								throw new I2B2DAOException(e.getMessage());
							}
							if (userLockedDate != null) {
								throw new LockedoutException(EJBPMUtil.LOCKEDOUT
										+ " error : User account locked on ["
										+ userLockedDate + "]");
							}
						}
					} else {
						log.debug("Setfinder not doing the lockout check for instance ["+ queryInstanceId + "] " +
								"the property value of query count is -1");
					}
				}
			} else {
				log
						.warn("No result output process to run, the <result_output_option> is empty");
			}

		} else {
			String resultType = defaultResultType;
			// perform patient set
			String resultInstanceId = getQueryResultInstanceId(sfDAOFactory,
					queryInstanceId, defaultResultType);
			param.put("ResultInstanceId", resultInstanceId);
			runGenerator(resultType, param);
			// check if the user need to be locked
			// if the lockoutQueryCount = -1 skip lockout check
			if (lockoutQueryCount>-1) {
				// resultInstanceId, userId
				if (dataObfuscFlag) {
					// do the check if the user need to be locked
					String userLockedDate = null;
					try {
						userLockedDate = processUserLock(sfDAOFactory, requestXml,
								resultInstanceId, lockoutQueryCount, lockoutQueryDay,lockoutQueryZeroCount);
					} catch (I2B2Exception e) {
						throw new I2B2DAOException(e.getMessage());
					}
					if (userLockedDate != null) {
						throw new LockedoutException(EJBPMUtil.LOCKEDOUT
								+ " error : User account locked on [" + userLockedDate
								+ "]");
					}
				}
			} else {
					log.debug("Setfinder not doing the lockout check for instance ["+ queryInstanceId + "] " +
							"the property value of query count is -1");
			}
		}
	}

	private void runGenerator(String resultName, Map param)
			throws I2B2DAOException, CRCTimeOutException {
		String generatorClassName = (String) generatorMap.get(resultName);
		if (generatorClassName == null) {
			throw new I2B2DAOException("Could not find result name ["
					+ resultName + "] in the config file");
		}
		Class generatorClass;
		IResultGenerator resultGenerator;
		try {
			generatorClass = Class.forName(generatorClassName, true, Thread
					.currentThread().getContextClassLoader());
			if (generatorClass == null) {
				throw new I2B2DAOException(
						"Generator class not configured for result name["
								+ resultName + "] ");
			}
			resultGenerator = (IResultGenerator) generatorClass.newInstance();
			log.debug("Running " + resultName + "'s class "
					+ generatorClassName);
			resultGenerator.generateResult(param);
		} catch (ClassNotFoundException e) {
			throw new I2B2DAOException(
					"Class not found for the generator class["
							+ generatorClassName + "] ", e);
		} catch (InstantiationException e) {
			throw new I2B2DAOException("Could not initialize generator class["
					+ generatorClassName + "] ", e);
		} catch (IllegalAccessException e) {
			throw new I2B2DAOException(
					"Illegal Access Exception for generator class["
							+ generatorClassName + "] ", e);
		}
	}

	private String getQueryResultInstanceId(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, String resultName) {
		IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		QtQueryResultInstance resultInstance = resultInstanceDao
				.getResultInstanceByQueryInstanceIdAndName(queryInstanceId,
						resultName);
		return resultInstance.getResultInstanceId();
	}

	private String processUserLock(SetFinderDAOFactory sfDAOFactory,
			String requestXml, String resultInstanceId, int lockoutQueryCount,
			int lockoutQueryDay, int lockoutQueryZeroCount) throws I2B2Exception {
		String userLockedDate = null;

		I2B2RequestMessageHelper reqMsgHelper = new I2B2RequestMessageHelper(
				requestXml);
		SecurityType origSecurityType = reqMsgHelper.getSecurityType();
		String projectId = reqMsgHelper.getProjectId();
		String userId = origSecurityType.getUsername();

		IQueryResultInstanceDao queryResultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		QtQueryResultInstance resultInstance = queryResultInstanceDao
				.getResultInstanceById(resultInstanceId);
		int resultTypeId = resultInstance.getQtQueryResultType()
				.getResultTypeId();
		int setSize = resultInstance.getRealSetSize();
		//if the lockout query zero count is -1, dont count zero
		if (setSize == 0 ) {
			if (lockoutQueryZeroCount == -1) {
				log.debug("Setfinder not doing the lockout check for result instance ["+ resultInstanceId + "] " +
						"the zero count property value is -1");
				return userLockedDate;
			} else { 
				//assign the lockout query zero count to lockout query count
				log.debug("Setfinder lockout query count value for result instance ["+ resultInstanceId + "] " +
						"taken from zero count property value is ["+lockoutQueryZeroCount +"]");
				lockoutQueryCount = lockoutQueryZeroCount;
			}
		}
		int queryCount = queryResultInstanceDao
				.getResultInstanceCountBySetSize(userId, lockoutQueryDay,
						resultTypeId, setSize, lockoutQueryCount);
		if (queryCount > 0) {
			userLockedDate = new Date(System.currentTimeMillis()).toString();
			SecurityType serviceSecurityType = PMServiceAccountUtil
					.getServiceSecurityType(origSecurityType.getDomain());

			// send pm message
			EJBPMUtil ejbPMUtil = new EJBPMUtil(serviceSecurityType, projectId);
			// EJBPMUtil ejbPMUtil = new EJBPMUtil(origSecurityType, projectId);

			ejbPMUtil.setUserLockedParam(userId, projectId,
					EJBPMUtil.LOCKEDOUT, userLockedDate);

		}

		return userLockedDate;
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
