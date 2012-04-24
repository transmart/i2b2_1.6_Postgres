package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;

public class QueryResultPatientSetGenerator extends CRCDAO implements
		IResultGenerator {

	public void generateResult(Map param) throws I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");
		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		String resultTypeName = (String) param.get("ResultOptionName");
		String processTimingFlag = (String) param.get("ProcessTimingFlag");
		int obfucatedRecordCount = (Integer) param.get("ObfuscatedRecordCount");
		DataSourceLookup originalDataSource = (DataSourceLookup) param
				.get("OriginalDataSourceLookup");
		List<String> roles = (List<String>) param.get("Roles");

		boolean errorFlag = false;
		Exception exception = null;
		int loadCount = 0, realCount = 0;
		String obfuscationDescription = "", obfusMethod = "";

		try {

			int i = 0;
			IPatientSetCollectionDao patientSetCollectionDao = sfDAOFactory
					.getPatientSetCollectionDAO();
			patientSetCollectionDao
					.createPatientSetCollection(resultInstanceId);
			
			String patientIdSql = "";			
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			
			// smuniraju: Separate block for Postgres to improve performance of INSERT queries.
			if(sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRES)) {
				String schemaName = sfDAOFactory.getDataSourceLookup().getFullSchema();		
				schemaName = (schemaName == null || schemaName.trim().equals("")) ? "" : getDbSchemaName() + ".";
				
				// Used to increment values for set_index
				java.sql.Statement sqlStmt = sfConn.createStatement();										
				sqlStmt.execute("CREATE TEMP SEQUENCE " + schemaName + "temp_seq_patient_set_index START WITH 1");
				sqlStmt.close();
				
				// Append resultInstanceId and nextVal(seq_qt_enc_col_set_index) to SELECT query obtained from buildEncounterSql				
				patientIdSql =  "SELECT DISTINCT patient_num, " + resultInstanceId + ", NEXTVAL('" + schemaName + "temp_seq_patient_set_index') " 
				             +  "FROM " + TEMP_DX_TABLE + " ORDER BY patient_num";								  
				String insertSql = "INSERT INTO " + schemaName + "qt_patient_set_collection(patient_num, result_instance_id, set_index)" 
								 + patientIdSql;			
				
				sqlStmt = sfConn.createStatement();
				logTimingUtil.setStartTime();
				loadCount = sqlStmt.executeUpdate(insertSql);
				logTimingUtil.setEndTime();
				sqlStmt.close();
				
			} else {
				patientIdSql = " select distinct patient_num from " + TEMP_DX_TABLE
                             + " order by patient_num ";
				Statement readQueryStmt = sfConn.createStatement();
				logTimingUtil.setStartTime();
				ResultSet resultSet = readQueryStmt.executeQuery(patientIdSql);
				while (resultSet.next()) {
					long patientNum = resultSet.getLong("patient_num");
					patientSetCollectionDao.addPatient(patientNum);
					i++;
					loadCount++;
	
					if ((i % 500) == 0) {
						log.debug("Loading [" + loadCount + "] patients"
								+ " for query instanse = " + queryInstanceId);
					}
				}
				readQueryStmt.close();
				patientSetCollectionDao.flush();
				logTimingUtil.setEndTime();
			}
			
			log.debug("Total patients loaded for query instance ="
					+ queryInstanceId + " is [" + loadCount + "]");
			
			if (processTimingFlag != null) {
				if (!processTimingFlag.trim().equalsIgnoreCase(ProcessTimingReportUtil.NONE) ) {
					ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
					ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil, "BUILD - PATIENTSET", ""));
				}
			}
			realCount = loadCount;
			/*
			 * DataSourceLookup dataSourceLookup = sfDAOFactory
			 * .getDataSourceLookup();
			 * this.setDbSchemaName(dataSourceLookup.getFullSchema()); String
			 * insertSql = "", generateRowNumSql = "";
			 * 
			 * if (dataSourceLookup.getServerType().equalsIgnoreCase(
			 * DAOFactoryHelper.ORACLE)) { insertSql = "insert into " +
			 * getDbSchemaName() +
			 * "qt_patient_set_collection(patient_set_coll_id,result_instance_id,set_index,patient_num) "
			 * + "select  " + getDbSchemaName() + "QT_SQ_QPR_PCID.nextval," +
			 * resultInstanceId + ",rownum,patient_num from " + TEMP_DX_TABLE +
			 * " order by patient_num"; } else if
			 * (dataSourceLookup.getServerType().equalsIgnoreCase(
			 * DAOFactoryHelper.SQLSERVER)) { generateRowNumSql =
			 * "select identity(int,1,1) rownumber,patient_num into #temp_dx from "
			 * + TEMP_DX_TABLE + " order by patient_num"; insertSql =
			 * "insert into " + getDbSchemaName() +
			 * "qt_patient_set_collection(result_instance_id,set_index,patient_num) select "
			 * + resultInstanceId + ", rownumber, patient_num from #temp_dx " +
			 * " order by patient_num drop table #temp_dx"; } Statement
			 * readQueryStmt = sfConn.createStatement(); if
			 * (generateRowNumSql.length() > 0) { log.debug("Executing SQL [" +
			 * generateRowNumSql + "]  for query instanse = " +
			 * queryInstanceId); readQueryStmt.executeUpdate(generateRowNumSql);
			 * } log.debug("Executing SQL [" + insertSql +
			 * "]  for query instanse = " + queryInstanceId); loadCount =
			 * readQueryStmt.executeUpdate(insertSql);
			 */

			// check for the user role to see if it needs data obfscation
			DataSourceLookup dataSourceLookup = sfDAOFactory
					.getOriginalDataSourceLookup();
			String domainId = originalDataSource.getDomainId();
			String projectId = originalDataSource.getProjectPath();
			String userId = originalDataSource.getOwnerId();
			boolean noDataAggFlag = false, noDataObfscFlag = false;

			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
					.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getOwnerId());

			IDAOFactory daoFactory = helper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);

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
				noDataObfscFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}

			if (noDataAggFlag && !noDataObfscFlag) {
				obfuscationDescription = "~";
				obfusMethod = IQueryResultInstanceDao.OBTOTAL;
				GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
				loadCount = (int) gaussianBoxMuller
						.getNormalizedValueForCount(loadCount);
			} else { 
				obfucatedRecordCount = loadCount;
			}

			// readQueryStmt.close();
		} catch (SQLException sqlEx) {
			exception = sqlEx;
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);

		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();
			
			String queryName = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
					sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, "",
						obfucatedRecordCount, 
						//loadCount, 
						realCount, obfusMethod);
				//String description = "Patient Set - " + obfuscationDescription
				//		+ loadCount + " Patients";
				String description = "Patient Set for \"" + queryName +"\"";
				resultInstanceDao.updateResultInstanceDescription(
						resultInstanceId, description);

			}
		}
	}
}
